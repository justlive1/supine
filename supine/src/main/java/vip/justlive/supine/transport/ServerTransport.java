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

package vip.justlive.supine.transport;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 服务端传输
 *
 * @author wubo
 */
public interface ServerTransport {

  /**
   * 启动
   *
   * @param host 主机
   * @param port 端口
   * @throws IOException io异常
   */
  default void start(String host, int port) throws IOException {
    start(new InetSocketAddress(host, port));
  }

  /**
   * 启动
   *
   * @param address 地址
   * @throws IOException io异常时抛出
   */
  void start(InetSocketAddress address) throws IOException;

  /**
   * 停止
   */
  void stop();
}
