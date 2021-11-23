/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vip.justlive.supine.registry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ExpiringMap;
import vip.justlive.oxygen.core.util.base.ExpiringMap.ExpiringPolicy;
import vip.justlive.oxygen.core.util.base.ExpiringMap.RemovalCause;
import vip.justlive.oxygen.core.util.net.aio.Client;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.transport.ClientTransport;
import vip.justlive.supine.transport.impl.AioClientTransport;
import vip.justlive.supine.transport.impl.ClientHandler;

/**
 * 抽象注册类
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractRegistry implements Registry {
  
  private static final Random RANDOM = new Random();
  protected Serializer serializer;
  private ExpiringMap<InetSocketAddress, ClientTransport> transports;
  private Client client;
  
  @Override
  public void start() throws IOException {
    GroupContext groupContext = new GroupContext(new ClientHandler(serializer));
    groupContext.setDaemon(true);
    client = new Client(groupContext);
  }
  
  @Override
  public void stop() {
    if (client != null) {
      client.close();
    }
  }
  
  void init(ClientConfig config) {
    this.transports = ExpiringMap.<InetSocketAddress, ClientTransport>builder()
        .expiration(config.getIdleTimeout(), TimeUnit.SECONDS)
        .expiringPolicy(ExpiringPolicy.ACCESSED).scheduleDelay(config.getIdleTimeout())
        .asyncExpiredListeners(this::expired).build();
  }
  
  ClientTransport load(List<InetSocketAddress> addresses, RequestKey key) {
    int size = addresses.size();
    int index = RANDOM.nextInt(size);
    AtomicReference<Exception> reference = new AtomicReference<>();
    for (int i = 0; i < size; i++) {
      InetSocketAddress address = addresses.get(index);
      ClientTransport transport = get(address, reference);
      index = (index + 1) % addresses.size();
      if (transport != null && !transport.isClosed() && transport.lookup(key) != null) {
        return transport;
      }
    }
    if (reference.get() != null) {
      throw Exceptions.wrap(reference.get());
    }
    throw Exceptions.fail("远程服务不可用");
  }
  
  private synchronized ClientTransport get(InetSocketAddress address,
      AtomicReference<Exception> reference) {
    ClientTransport transport = transports.get(address);
    if (transport != null && !transport.isClosed()) {
      return transport;
    }
    transport = new AioClientTransport(client, serializer);
    try {
      transport.connect(address);
      transports.put(address, transport);
      return transport;
    } catch (Exception e) {
      log.warn("客户端连接[{}]服务失败", address, e);
      reference.set(e);
    }
    return null;
  }
  
  private void expired(InetSocketAddress address, ClientTransport transport, RemovalCause cause) {
    if (log.isDebugEnabled()) {
      log.debug("[{}]连接失效[{}]，关闭连接", cause, address);
    }
    transport.close();
  }
}
