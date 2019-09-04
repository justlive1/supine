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

package vip.justlive.supine.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import vip.justlive.oxygen.core.util.SnowflakeIdWorker;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.Request;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.ResultFuture;
import vip.justlive.supine.registry.Registry;
import vip.justlive.supine.transport.ClientTransport;

/**
 * 客户端服务接口代理
 *
 * @author wubo
 */
@Data
public class ReferenceProxy implements InvocationHandler {

  private final ClientConfig config;
  private final Registry registry;
  private final String version;

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    if (Object.class.equals(method.getDeclaringClass())) {
      return method.invoke(this, args);
    }

    Request request = new Request().setVersion(version)
        .setClassName(method.getDeclaringClass().getName()).setMethodName(method.getName())
        .setArgTypes(method.getParameterTypes()).setArgs(args)
        .setId(SnowflakeIdWorker.defaultNextId());

    ClientTransport transport = registry.discovery(
        new RequestKey(version, request.getClassName(), request.getMethodName(),
            request.getArgTypes()));

    ResultFuture<?> resultFuture = new ResultFuture<>(method.getReturnType());

    if (config.isAsync()) {
      return async(request, resultFuture, transport);
    } else {
      return sync(request, resultFuture, transport);
    }
  }

  private Object sync(Request request, ResultFuture<?> resultFuture, ClientTransport transport)
      throws Throwable {
    ResultFuture.add(request.getId(), resultFuture);
    try {
      transport.send(request);
      return resultFuture.get(config.getTimeout(), TimeUnit.SECONDS);
    } finally {
      ResultFuture.remove(request.getId());
    }
  }

  private Object async(Request request, ResultFuture<?> resultFuture, ClientTransport transport) {
    ResultFuture.add(request.getId(), resultFuture);
    resultFuture.local();
    try {
      transport.send(request);
    } catch (Exception e) {
      ResultFuture.future();
      throw e;
    }
    return null;
  }
}
