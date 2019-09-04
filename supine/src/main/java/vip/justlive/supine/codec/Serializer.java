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

package vip.justlive.supine.codec;

/**
 * 序列化接口
 *
 * @author wubo
 */
public interface Serializer {

  /**
   * 默认实现
   *
   * @return serializer
   */
  static Serializer def() {
    return KryoSerializer.INSTANCE;
  }

  /**
   * 序列化
   *
   * @param obj 对象
   * @return 字节数组
   */
  byte[] encode(Object obj);

  /**
   * 反序列化
   *
   * @param bytes 字节数组
   * @return 对象
   */
  Object decode(byte[] bytes);
}
