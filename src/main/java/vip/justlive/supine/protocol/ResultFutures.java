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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;

/**
 * 结果futures
 *
 * @author wubo
 */
@UtilityClass
public class ResultFutures {

  private static final Map<Long, CompletableFuture<Response>> FUTURES = new ConcurrentHashMap<>();

  /**
   * 添加future
   *
   * @param id 请求id
   * @param future CompletableFuture
   */
  public static void add(Long id, CompletableFuture<Response> future) {
    FUTURES.put(id, future);
  }

  /**
   * 完成CompletableFuture
   *
   * @param response 响应
   */
  public static void complete(Response response) {
    if (response == null) {
      return;
    }
    CompletableFuture<Response> future = FUTURES.get(response.getId());
    if (future != null) {
      future.complete(response);
    }
  }

  /**
   * 删除
   *
   * @param id 请求id
   */
  public static void remove(Long id) {
    FUTURES.remove(id);
  }
}
