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

package vip.justlive.supine.router;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.supine.common.ClientConfig;

/**
 * 直连router
 *
 * @author wubo
 */
@Slf4j
public class DirectRouter extends AbstractRouter {

  public DirectRouter(ClientConfig config) {
    super(config.getIdleTimeout());
    List<InetSocketAddress> addresses = new LinkedList<>();
    for (String address : config.getRegistryAddress().trim().split(Constants.COMMA)) {
      String[] hostPort = address.trim().split(Constants.COLON);
      addresses.add(new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1])));
    }
    socketAddresses.addAll(addresses);
  }

}
