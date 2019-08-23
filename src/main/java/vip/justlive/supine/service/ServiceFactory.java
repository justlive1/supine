/*
 * Copyright (C) 2019 justlive1
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

package vip.justlive.supine.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.protocol.Request;
import vip.justlive.supine.protocol.Response;

/**
 * 服务代理工厂
 *
 * @author wubo
 */
public class ServiceFactory {

  private static final Map<ServiceMethodKey, ServiceMethodInvoker> SERVICES = new HashMap<>(4);

  public static void register(Object service) {
    register(service, Constants.EMPTY);
  }

  public static void register(Object service, String version) {
    Class<?> serviceType = service.getClass();
    Class<?>[] interfaces = serviceType.getInterfaces();
    if (interfaces == null || interfaces.length == 0) {
      return;
    }
    for (Class<?> intf : interfaces) {
      if (intf.getClassLoader() != null && intf.getName().startsWith("java.")) {
        continue;
      }
      register(intf, service);
    }
  }

  public static void register(Class<?> interfaceType, Object service) {
    register(interfaceType, service, Constants.EMPTY);
  }

  public static void register(Class<?> interfaceType, Object service, String version) {
    Method[] methods = interfaceType.getDeclaredMethods();
    if (methods == null || methods.length == 0) {
      return;
    }
    Class<?> clazz = service.getClass();
    try {
      for (Method method : methods) {
        Method realMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        SERVICES.put(new ServiceMethodKey(version, interfaceType.getName(), method.getName(),
            method.getParameterTypes()), new ServiceMethodInvoker(service, realMethod));
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static Response invoke(Request request) {
    ServiceMethodKey key = new ServiceMethodKey(request.getVersion(), request.getClassName(),
        request.getMethodName(), request.getArgTypes());
    ServiceMethodInvoker invoker = SERVICES.get(key);
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
    return response;
  }
}
