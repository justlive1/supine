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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.Client;
import vip.justlive.oxygen.core.util.net.aio.LengthFrame;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.common.Request;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.RequestKeyWrapper;
import vip.justlive.supine.transport.ClientTransport;

/**
 * aio实现传输
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class AioClientTransport implements ClientTransport {
  
  private final Client client;
  private final Serializer serializer;
  private ChannelContext channel;
  
  @Override
  public void connect(InetSocketAddress address) throws IOException {
    this.channel = client.connect(address);
    Transport transport = new Transport(channel);
    this.channel.addAttr(Transport.class.getName(), transport);
    try {
      transport.join(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    } catch (ExecutionException | TimeoutException e) {
      throw new IOException(e);
    }
  }
  
  @Override
  public void close() {
    if (channel != null) {
      channel.close();
    }
  }
  
  @Override
  public boolean isClosed() {
    return channel == null || channel.isClosed();
  }
  
  @Override
  public void send(Request request) {
    channel.write(new LengthFrame().setType(Transport.REQUEST).setBody(serializer.encode(request)));
  }
  
  @Override
  public Integer lookup(RequestKey key) {
    if (isClosed()) {
      return null;
    }
    RequestKeyWrapper wrapper = (RequestKeyWrapper) channel.getAttr(RequestKey.class.getName());
    if (wrapper != null) {
      return wrapper.lookup(key);
    }
    return null;
  }
}
