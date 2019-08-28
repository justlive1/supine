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

package vip.justlive.supine.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * 服务方法key
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class RequestKey implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 版本号
   */
  private final String version;
  /**
   * 类名
   */
  private final String className;
  /**
   * 方法名
   */
  private final String methodName;
  /**
   * 参数类型
   */
  private final Class<?>[] types;

  @Override
  public int hashCode() {
    return Objects.hashCode(version) + Objects.hashCode(className) + Objects.hashCode(methodName)
        + Arrays.hashCode(types);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof RequestKey)) {
      return false;
    }
    RequestKey other = (RequestKey) obj;
    return Objects.equals(version, other.version) && Objects.equals(className, other.className)
        && Objects.equals(methodName, other.methodName) && Arrays.equals(types, other.types);
  }

}
