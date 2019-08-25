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

import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrame;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrameHandler;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.protocol.Request;
import vip.justlive.supine.protocol.Response;
import vip.justlive.supine.service.ServiceMethodInvoker;
import vip.justlive.supine.service.ServiceMethodKey;

/**
 * 服务端消息处理
 *
 * @author wubo
 */
public class ServerHandler extends LengthFrameHandler {


  @Override
  public void handle(Object data, ChannelContext channelContext) {
    LengthFrame frame = (LengthFrame) data;
    Request request = (Request) Serializer.def().decode(frame.getBody());
    ServiceMethodKey key = new ServiceMethodKey(request.getVersion(), request.getClassName(),
        request.getMethodName(), request.getArgTypes());
    ServiceMethodInvoker invoker = ServiceMethodInvoker.lookup(key);
    Response response = new Response().setId(request.getId());
    if (invoker == null) {
      response.setException(Exceptions.fail("service not found"));
    } else {
      try {
        response.setResult(invoker.invoke(request.getArgs()));
      } catch (Throwable e) {
        response.setException(e);
      }
    }
    channelContext.write(new LengthFrame().setType(2).setBody(Serializer.def().encode(response)));
  }
}
