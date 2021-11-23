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

import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.LengthFrame;
import vip.justlive.oxygen.core.util.net.aio.LengthFrameHandler;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.Response;
import vip.justlive.supine.common.ResultFuture;

/**
 * 客户端消息处理
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ClientHandler extends LengthFrameHandler {
  
  private final Serializer serializer;
  
  @Override
  public void handle(Object data, ChannelContext channelContext) {
    LengthFrame frame = (LengthFrame) data;
    Object obj = serializer.decode(frame.getBody());
    if (frame.getType() == Transport.ENDPOINT) {
      Transport transport = (Transport) channelContext.removeAttr(Transport.class.getName());
      if (transport != null) {
        channelContext.addAttr(RequestKey.class.getName(), obj);
        transport.complete();
      }
      return;
    }
    Response response = (Response) obj;
    ResultFuture.complete(response);
  }
}
