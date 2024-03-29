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

package vip.justlive.supine.transport.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;
import vip.justlive.oxygen.core.util.net.aio.Server;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.transport.ServerTransport;

/**
 * aio实现传输
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class AioServerTransport implements ServerTransport {
  
  private final Serializer serializer;
  private Server server;
  
  @Override
  public void start(InetSocketAddress address) throws IOException {
    ServerHandler handler = new ServerHandler(serializer);
    GroupContext groupContext = new GroupContext(handler).setAioListener(handler);
    server = new Server(groupContext);
    server.start(address);
  }
  
  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
  
}
