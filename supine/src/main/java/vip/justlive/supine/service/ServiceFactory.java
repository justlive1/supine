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

package vip.justlive.supine.service;

import java.io.IOException;
import java.lang.reflect.Method;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.Strings;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.registry.MulticastRegistry;
import vip.justlive.supine.registry.Registry;
import vip.justlive.supine.transport.ServerTransport;
import vip.justlive.supine.transport.impl.AioServerTransport;

/**
 * 服务代理工厂
 *
 * @author wubo
 */
public class ServiceFactory {

  private final ServiceConfig config;
  private final Registry registry;

  private ServerTransport transport;
  private volatile boolean state;

  public ServiceFactory(ServiceConfig config) {
    this(config, select(config));
  }

  public ServiceFactory(ServiceConfig config, Registry registry) {
    this.config = config;
    this.registry = registry;
  }

  private static Registry select(ServiceConfig config) {
    if (config.getRegistryType() == 1) {
      return new MulticastRegistry(config);
    }
    return null;
  }

  /**
   * 注册服务
   *
   * @param service 服务实现
   */
  public void register(Object service) {
    Service annotation = ClassUtils
        .getAnnotation(ClassUtils.getCglibActualClass(service.getClass()), Service.class);
    if (annotation != null) {
      register(service, annotation.version());
    } else {
      register(service, Strings.EMPTY);
    }
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
    register(interfaceType, service, Strings.EMPTY);
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
        ServiceMethodInvoker.add(new RequestKey(version, interfaceType.getName(), method.getName(),
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
  public void start() throws IOException {
    if (state) {
      return;
    }
    state = true;
    transport = new AioServerTransport();
    transport.start(config.getHost(), config.getPort());
    if (registry != null) {
      registry.register(ServiceMethodInvoker.requestKeys());
      registry.start();
    }
  }

  /**
   * 停止
   */
  public void stop() {
    if (!state) {
      return;
    }
    state = false;
    if (registry != null) {
      registry.stop();
    }
    transport.stop();
    ServiceMethodInvoker.clear();
  }
}
