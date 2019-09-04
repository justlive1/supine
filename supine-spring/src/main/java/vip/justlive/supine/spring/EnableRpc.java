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

package vip.justlive.supine.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import vip.justlive.supine.spring.client.ReferenceBeanPostProcessor;
import vip.justlive.supine.spring.service.ContextEventListener;

/**
 * 开启rpc
 *
 * @author wubo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ContextEventListener.class, ReferenceBeanPostProcessor.class})
public @interface EnableRpc {

  /**
   * 超时时间
   */
  String TIMEOUT = "supine.common.timeout";
  /**
   * 注册中心类型
   */
  String REGISTRY_TYPE = "supine.common.registryType";
  /**
   * 注册中心地址
   */
  String REGISTRY_ADDRESS = "supine.common.registryAddress";
  /**
   * 服务端主机名称
   */
  String SERVER_HOST = "supine.server.host";
  /**
   * 服务端端口
   */
  String SERVER_PORT = "supine.server.port";
  /**
   * 客户端是否开启异步，默认false
   */
  String CLIENT_ASYNC = "supine.client.async";
  /**
   * 客户端空闲超时时间
   */
  String CLIENT_IDLE_TIMEOUT = "supine.client.idleTimeout";
}
