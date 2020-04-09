/*
 * Copyright (C) 2020 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * request Key wrapper
 *
 * @author wubo
 */
public class RequestKeyWrapper {

  private final Map<RequestKey, Integer> keys;

  public RequestKeyWrapper(List<RequestKey> list) {
    keys = new HashMap<>(4);
    list.forEach(key -> keys.put(key, key.getId()));
  }

  public Integer lookup(RequestKey key) {
    return keys.get(key);
  }
}
