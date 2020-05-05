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

import lombok.Getter;
import lombok.Setter;
import vip.justlive.supine.codec.Serializer;

/**
 * 配置
 *
 * @author wubo
 */
@Getter
@Setter
public class Config {

  /**
   * 超时时间，单位秒
   */
  private int timeout = 5;

  /**
   * 注册类型，0：直连
   */
  private int registryType = 0;
  /**
   * 注册地址
   */
  private String registryAddress;

  private Serializer serializer;

}
