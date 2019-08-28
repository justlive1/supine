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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.transport.ClientTransport;
import vip.justlive.supine.transport.impl.AioClientTransport;

/**
 * 抽象注册类
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractRegistry implements Registry {

  private static final Random RANDOM = new Random();
  private ExpiringMap<InetSocketAddress, ClientTransport> transports;

  void init(ClientConfig config) {
    this.transports = ExpiringMap.<InetSocketAddress, ClientTransport>builder()
        .expiration(config.getIdleTimeout(), TimeUnit.SECONDS)
        .scheduleDelay(config.getIdleTimeout()).asyncExpiredListeners(this::expired).build();
  }

  ClientTransport load(List<InetSocketAddress> addresses) {
    int size = addresses.size();
    int index = RANDOM.nextInt(size);
    for (int i = 0; i < size; i++) {
      InetSocketAddress address = addresses.get(index);
      ClientTransport transport = get(address);
      index = (index + 1) % addresses.size();
      if (transport != null && !transport.isClosed()) {
        return transport;
      }
    }
    throw Exceptions.fail("远程服务不可用");
  }

  private synchronized ClientTransport get(InetSocketAddress address) {
    ClientTransport transport = transports.get(address);
    if (transport != null && !transport.isClosed()) {
      return transport;
    }
    transport = new AioClientTransport();
    try {
      transport.connect(address);
      transports.put(address, transport);
      return transport;
    } catch (Exception e) {
      log.warn("客户端连接[{}]服务失败", address, e);
    }
    return null;
  }

  private void expired(InetSocketAddress address, ClientTransport transport) {
    if (log.isDebugEnabled()) {
      log.debug("[{}]连接空闲超时，关闭连接", address);
    }
    transport.close();
  }
}
