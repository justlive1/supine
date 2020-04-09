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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import vip.justlive.supine.common.RequestKey;

/**
 * 服务调用类
 *
 * @author wubo
 */
@Data
public class ServiceMethodInvoker {

  private static final Map<RequestKey, ServiceMethodInvoker> SERVICES = new HashMap<>(4);
  private static final Map<Integer, RequestKey> KEYS = new HashMap<>(4);

  private final Object service;
  private final Method method;

  /**
   * 添加服务调用
   *
   * @param key 服务方法签名
   * @param service 服务
   * @param method 方法
   */
  static void add(RequestKey key, Object service, Method method) {
    SERVICES.put(key, new ServiceMethodInvoker(service, method));
    KEYS.put(key.getId(), key);
  }

  /**
   * 查找服务调用方法
   *
   * @param id 服务方法id
   * @return 服务调用
   */
  public static ServiceMethodInvoker lookup(Integer id) {
    RequestKey key = KEYS.get(id);
    if (key != null) {
      return SERVICES.get(key);
    }
    return null;
  }

  /**
   * 获取所有服务调用key
   *
   * @return keys
   */
  public static List<RequestKey> requestKeys() {
    return new ArrayList<>(SERVICES.keySet());
  }

  /**
   * 删除
   */
  static void clear() {
    SERVICES.clear();
    KEYS.clear();
  }

  /**
   * 服务调用
   *
   * @param args 参数
   * @return 结果
   * @throws InvocationTargetException 反射异常
   * @throws IllegalAccessException 反射异常
   */
  public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
    return method.invoke(service, args);
  }

}
