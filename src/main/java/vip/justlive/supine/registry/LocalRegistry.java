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

package vip.justlive.supine.registry;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.SystemUtils;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.transport.ClientTransport;

/**
 * 本地注册
 *
 * @author wubo
 */
public class LocalRegistry extends AbstractRegistry {

  private final List<InetSocketAddress> socketAddresses = new ArrayList<>();

  public LocalRegistry(ClientConfig config) {
    init(config);
    for (String address : config.getRegistryAddress().trim().split(Constants.COMMA)) {
      socketAddresses.add(SystemUtils.parseAddress(address));
    }
  }

  @Override
  public void register(List<RequestKey> keys) {
    // nothing
  }

  @Override
  public void unregister(List<RequestKey> keys) {
    // nothing
  }

  @Override
  public void start() {
    // nothing
  }

  @Override
  public void stop() {
    // nothing
  }

  @Override
  public ClientTransport discovery(RequestKey key) {
    int size = socketAddresses.size();
    if (size == 0) {
      throw Exceptions.fail("没有可用的服务提供者");
    }
    return load(socketAddresses);
  }
}
