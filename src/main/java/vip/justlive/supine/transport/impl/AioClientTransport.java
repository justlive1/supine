/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.supine.transport.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import lombok.Data;
import vip.justlive.oxygen.core.net.aio.core.Client;
import vip.justlive.oxygen.core.net.aio.core.GroupContext;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrame;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.protocol.Request;
import vip.justlive.supine.transport.ClientTransport;

/**
 * aio实现传输
 *
 * @author wubo
 */
@Data
public class AioClientTransport implements ClientTransport {

  private Client client;

  @Override
  public void connect(InetSocketAddress address) throws IOException {
    GroupContext groupContext = new GroupContext(new ClientHandler());
    client = new Client(groupContext);
    client.connect(address);
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
    }
  }

  @Override
  public boolean isClosed() {
    return client != null && !client.getGroupContext().isStopped();
  }

  @Override
  public void send(Request request) {
    client.write(new LengthFrame().setType(1).setBody(Serializer.def().encode(request)));
  }
}
