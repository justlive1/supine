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

import java.util.Arrays;
import java.util.Objects;
import lombok.Data;

/**
 * 服务方法key
 *
 * @author wubo
 */
@Data
public class ServiceMethodKey {

  private final String version;
  private final String className;
  private final String methodName;
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
    if (!(obj instanceof ServiceMethodKey)) {
      return false;
    }
    ServiceMethodKey other = (ServiceMethodKey) obj;
    return Objects.equals(version, other.version) && Objects.equals(className, other.className)
        && Objects.equals(methodName, other.methodName) && Arrays.equals(types, other.types);
  }

}
