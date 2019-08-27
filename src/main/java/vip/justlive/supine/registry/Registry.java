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

package vip.justlive.supine.registry;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.transport.ClientTransport;

/**
 * 服务注册
 *
 * @author wubo
 */
public interface Registry {

  /**
   * 注册方法调用key
   *
   * @param key 方法调用key
   */
  default void register(RequestKey key) {
    register(Collections.singletonList(key));
  }

  /**
   * 注册方法调用keys
   *
   * @param keys 方法调用keys
   */
  void register(List<RequestKey> keys);

  /**
   * 注销方法调用key
   *
   * @param key 方法调用key
   */
  default void unregister(RequestKey key) {
    unregister(Collections.singletonList(key));
  }

  /**
   * 注销方法调用key
   *
   * @param keys 方法调用keys
   */
  void unregister(List<RequestKey> keys);

  /**
   * 启动
   *
   * @throws IOException io异常时抛出
   */
  void start() throws IOException;

  /**
   * 停止
   */
  void stop();

  /**
   * 发现
   *
   * @param key 方法调用key
   * @return ClientTransport
   */
  ClientTransport discovery(RequestKey key);
}
