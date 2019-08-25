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

package vip.justlive.supine.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务端配置
 *
 * @author wubo
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceConfig extends Config {

  /**
   * 绑定主机
   */
  private final String host;
  /**
   * 绑定端口
   */
  private final int port;
}
