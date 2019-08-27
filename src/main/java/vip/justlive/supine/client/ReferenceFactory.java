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

import java.io.IOException;
import java.lang.reflect.Proxy;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.registry.LocalRegistry;
import vip.justlive.supine.registry.MulticastRegistry;
import vip.justlive.supine.registry.Registry;

/**
 * 客户端服务工厂
 *
 * @author wubo
 */
public class ReferenceFactory {

  private final ClientConfig config;
  private final Registry registry;
  private volatile boolean state;

  public ReferenceFactory(ClientConfig config) {
    this(config, select(config));
  }

  public ReferenceFactory(ClientConfig config, Registry registry) {
    this.config = config;
    this.registry = registry;
  }

  private static Registry select(ClientConfig config) {
    if (config.getRegistryType() == 1) {
      return new MulticastRegistry(config);
    }
    if (config.getRegistryAddress() == null || config.getRegistryAddress().trim().isEmpty()) {
      throw Exceptions.fail("[registryAddress]不正确");
    }
    return new LocalRegistry(config);
  }

  /**
   * 创建服务代理
   *
   * @param referenceType 需要创建的接口类
   * @param <T> 接口类型
   * @return bean
   */
  public <T> T create(Class<T> referenceType) {
    MoreObjects.notNull(referenceType, "[referenceType]不能为空");
    if (!referenceType.isInterface()) {
      throw Exceptions.fail(String.format("[%s]不是接口类型", referenceType));
    }
    Reference reference = ClassUtils.getAnnotation(referenceType, Reference.class);
    if (reference != null) {
      return create(referenceType, reference.version());
    }
    return create(referenceType, Constants.EMPTY);
  }

  /**
   * 创建服务代理
   *
   * @param referenceType 需要创建的接口类
   * @param version 服务版本
   * @param <T> 接口类型
   * @return bean
   */
  public <T> T create(Class<T> referenceType, String version) {
    return referenceType.cast(Proxy
        .newProxyInstance(referenceType.getClassLoader(), new Class[]{referenceType},
            new ReferenceProxy(config, registry, version)));
  }

  /**
   * 启动
   */
  public void start() throws IOException {
    if (state) {
      return;
    }
    state = true;
    if (registry != null) {
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
  }
}
