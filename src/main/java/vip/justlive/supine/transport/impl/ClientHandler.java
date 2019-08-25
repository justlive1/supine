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

import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrame;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrameHandler;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.protocol.Response;
import vip.justlive.supine.protocol.ResultFutures;

/**
 * 客户端消息处理
 *
 * @author wubo
 */
public class ClientHandler extends LengthFrameHandler {

  @Override
  public void handle(Object data, ChannelContext channelContext) {
    LengthFrame frame = (LengthFrame) data;
    Response response = (Response) Serializer.def().decode(frame.getBody());
    ResultFutures.complete(response);
  }
}
