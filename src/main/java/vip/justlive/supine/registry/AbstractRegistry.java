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
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
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

  private ExpiringMap<InetSocketAddress, ClientTransport> transports;

  void init(ClientConfig config) {
    this.transports = ExpiringMap.<InetSocketAddress, ClientTransport>builder()
        .expiration(config.getIdleTimeout(), TimeUnit.SECONDS)
        .scheduleDelay(config.getIdleTimeout()).asyncExpiredListeners(this::expired).build();
  }

  synchronized ClientTransport get(InetSocketAddress address) {
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
      log.warn("connect to {} error", address, e);
    }
    return null;
  }

  private void expired(InetSocketAddress address, ClientTransport transport) {
    if (log.isDebugEnabled()) {
      log.debug("{} expired. close transport", address);
    }
    transport.close();
  }
}
