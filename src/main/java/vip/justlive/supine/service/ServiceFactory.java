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

import java.io.IOException;
import java.lang.reflect.Method;
import lombok.Data;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.protocol.ServiceConfig;
import vip.justlive.supine.transport.ServerTransport;
import vip.justlive.supine.transport.impl.AioServerTransport;

/**
 * 服务代理工厂
 *
 * @author wubo
 */
@Data
public class ServiceFactory {

  private final ServiceConfig config;

  private ServerTransport transport;
  private volatile boolean state;

  /**
   * 注册服务
   *
   * @param service 服务实现
   */
  public void register(Object service) {
    register(service, Constants.EMPTY);
  }

  /**
   * 注册服务
   *
   * @param service 服务实现
   * @param version 服务版本
   */
  public void register(Object service, String version) {
    Class<?> serviceType = service.getClass();
    Class<?>[] interfaces = serviceType.getInterfaces();
    if (interfaces == null || interfaces.length == 0) {
      return;
    }
    for (Class<?> intf : interfaces) {
      if (intf.getClassLoader() != null && intf.getName().startsWith("java.")) {
        continue;
      }
      register(intf, service, version);
    }
  }

  /**
   * 注册服务，指定接口
   *
   * @param interfaceType 接口类型
   * @param service 服务实现
   */
  public void register(Class<?> interfaceType, Object service) {
    register(interfaceType, service, Constants.EMPTY);
  }

  /**
   * 注册服务，指定接口和版本
   *
   * @param interfaceType 接口类型
   * @param service 服务实现
   * @param version 版本
   */
  public void register(Class<?> interfaceType, Object service, String version) {
    Method[] methods = interfaceType.getDeclaredMethods();
    if (methods.length == 0) {
      return;
    }
    Class<?> clazz = service.getClass();
    try {
      for (Method method : methods) {
        Method realMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        ServiceMethodInvoker.add(
            new ServiceMethodKey(version, interfaceType.getName(), method.getName(),
                method.getParameterTypes()), service, realMethod);
      }
    } catch (NoSuchMethodException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * 启动
   *
   * @throws IOException io异常时抛出
   */
  public synchronized void start() throws IOException {
    if (state) {
      return;
    }
    state = true;
    transport = new AioServerTransport();
    transport.start(config.getHost(), config.getPort());
  }

  /**
   * 停止
   */
  public synchronized void stop() {
    if (!state) {
      return;
    }
    state = false;
    transport.stop();
    ServiceMethodInvoker.clear();
  }
}
