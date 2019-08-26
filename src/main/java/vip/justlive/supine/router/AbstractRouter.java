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

package vip.justlive.supine.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.supine.common.Request;
import vip.justlive.supine.transport.ClientTransport;
import vip.justlive.supine.transport.impl.AioClientTransport;

/**
 * 抽象Router
 *
 * @author wubo
 */
@Slf4j
public abstract class AbstractRouter implements Router {

  final List<InetSocketAddress> socketAddresses = new CopyOnWriteArrayList<>();
  private final ExpiringMap<InetSocketAddress, ClientTransport> transports;
  private int index = -1;
  private final Random random = new Random();

  AbstractRouter(int idleTimeout) {
    transports = ExpiringMap.<InetSocketAddress, ClientTransport>builder()
        .expiration(idleTimeout, TimeUnit.SECONDS).scheduleDelay(idleTimeout)
        .asyncExpiredListeners(this::expired).build();
  }

  @Override
  public ClientTransport route(Request request) {
    int size = socketAddresses.size();
    if (size == 0) {
      throw Exceptions.fail("没有可用的服务提供者");
    }
    if (index == -1) {
      index = random.nextInt(size);
    }
    InetSocketAddress address = socketAddresses.get(index);
    ClientTransport transport = get(address);

    index = (index + 1) % socketAddresses.size();
    return transport;
  }

  private synchronized ClientTransport get(InetSocketAddress address) {
    ClientTransport transport = transports.get(address);
    if (transport != null && !transport.isClosed()) {
      return transport;
    }
    transport = new AioClientTransport();
    try {
      transport.connect(address);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    transports.put(address, transport);
    return transport;
  }

  private void expired(InetSocketAddress address, ClientTransport transport) {
    if (log.isDebugEnabled()) {
      log.debug("{} expired. close transport", address);
    }
    transport.close();
  }
}
