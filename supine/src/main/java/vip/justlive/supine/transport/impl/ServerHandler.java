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
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.exception.WrappedException;
import vip.justlive.oxygen.core.util.net.aio.AioListener;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.LengthFrame;
import vip.justlive.oxygen.core.util.net.aio.LengthFrameHandler;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.common.Request;
import vip.justlive.supine.common.RequestKeyWrapper;
import vip.justlive.supine.common.Response;
import vip.justlive.supine.service.ServiceMethodInvoker;

/**
 * 服务端消息处理
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class ServerHandler extends LengthFrameHandler implements AioListener {
  
  private final Serializer serializer;
  
  @Override
  public void onConnected(ChannelContext channelContext) {
    channelContext.write(new LengthFrame().setType(Transport.ENDPOINT)
        .setBody(serializer.encode(new RequestKeyWrapper(ServiceMethodInvoker.requestKeys()))));
  }
  
  @Override
  public void handle(Object data, ChannelContext channelContext) {
    LengthFrame frame = (LengthFrame) data;
    if (frame.getType() == Transport.BEAT) {
      // 心跳请求不处理
      return;
    }
    Request request = (Request) serializer.decode(frame.getBody());
    ServiceMethodInvoker invoker = ServiceMethodInvoker.lookup(request.getMid());
    Response response = new Response().setId(request.getId());
    if (invoker == null) {
      log.warn("not found service for {} in {}", request, ServiceMethodInvoker.requestKeys());
      response.setException(Exceptions.fail("远程服务没有对应版本的实现"));
    } else {
      try {
        response.setResult(invoker.invoke(request.getArgs()));
      } catch (WrappedException e) {
        response.setException(e.getException());
      } catch (Throwable e) {
        response.setException(e);
      }
    }
    channelContext
        .write(new LengthFrame().setType(Transport.RESPONSE).setBody(serializer.encode(response)));
  }
}
