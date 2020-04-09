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

package vip.justlive.supine.transport.impl;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;

/**
 * 传输工具类
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class Transport {

  public static final int BEAT = -1;
  public static final int REQUEST = 1;
  public static final int RESPONSE = 2;
  public static final int ENDPOINT = 3;

  private final ChannelContext channel;
  private CompletableFuture<Void> future = new CompletableFuture<>();

  public void join() {
    if (channel != null) {
      channel.join();
    }
    if (future != null) {
      future.join();
    }
  }

  public void complete() {
    if (future != null) {
      future.complete(null);
    }
  }
}
