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

package vip.justlive.supine.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * 服务调用类
 *
 * @author wubo
 */
@Data
public class ServiceMethodInvoker {

  private static final Map<ServiceMethodKey, ServiceMethodInvoker> SERVICES = new HashMap<>(4);

  private final Object service;
  private final Method method;

  /**
   * 添加服务调用
   *
   * @param key 服务方法签名
   * @param service 服务
   * @param method 方法
   */
  public static void add(ServiceMethodKey key, Object service, Method method) {
    SERVICES.put(key, new ServiceMethodInvoker(service, method));
  }

  /**
   * 查找服务调用方法
   *
   * @param key 服务方法起那么
   * @return 服务调用
   */
  public static ServiceMethodInvoker lookup(ServiceMethodKey key) {
    return SERVICES.get(key);
  }

  /**
   * 删除
   */
  public static void clear() {
    SERVICES.clear();
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
