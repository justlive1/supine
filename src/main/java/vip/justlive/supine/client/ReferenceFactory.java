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

package vip.justlive.supine.client;

import java.lang.reflect.Proxy;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * 客户端服务工厂
 *
 * @author wubo
 */
public class ReferenceFactory {

  /**
   * 创建服务代理
   *
   * @param referenceType 需要创建的接口类
   * @param <T> 接口类型
   * @return bean
   */
  public static <T> T create(Class<T> referenceType) {
    MoreObjects.notNull(referenceType, "referenceType不能为空");
    if (!referenceType.isInterface()) {
      throw Exceptions.fail(String.format("%s不是接口类型", referenceType));
    }
    Reference reference = referenceType.getAnnotation(Reference.class);
    if (reference != null) {
      return create(referenceType, reference.address(), reference.version());
    }
    return create(referenceType, Constants.EMPTY);
  }

  /**
   * 创建服务代理
   *
   * @param referenceType 需要创建的接口类
   * @param address 远程服务地址
   * @param <T> 接口类型
   * @return bean
   */
  public static <T> T create(Class<T> referenceType, String address) {
    return create(referenceType, address, Constants.EMPTY);
  }

  /**
   * 创建服务代理
   *
   * @param referenceType 需要创建的接口类
   * @param address 远程服务地址
   * @param version 服务版本
   * @param <T> 接口类型
   * @return bean
   */
  public static <T> T create(Class<T> referenceType, String address, String version) {
    return referenceType.cast(Proxy
        .newProxyInstance(referenceType.getClassLoader(), new Class[]{referenceType},
            new ReferenceProxy(address, version)));
  }
}
