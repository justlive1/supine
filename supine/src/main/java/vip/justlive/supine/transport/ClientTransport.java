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
import vip.justlive.supine.common.Request;
import vip.justlive.supine.common.RequestKey;

/**
 * 客户端传输
 *
 * @author wubo
 */
public interface ClientTransport {

  /**
   * 连接服务器
   *
   * @param address 地址
   * @throws IOException io异常时抛出
   */
  void connect(InetSocketAddress address) throws IOException;

  /**
   * 关闭
   */
  void close();

  /**
   * 是否已关闭
   *
   * @return true为停止
   */
  boolean isClosed();

  /**
   * 发送请求
   *
   * @param request 请求
   */
  void send(Request request);

  /**
   * 获取方法id
   *
   * @param key 请求签名
   * @return mid
   */
  Integer lookup(RequestKey key);
}
