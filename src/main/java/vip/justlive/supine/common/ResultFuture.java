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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 结果future
 *
 * @author wubo
 */
@Accessors(chain = true)
public class ResultFuture<T> {

  private static final Map<Long, ResultFuture<?>> FUTURES = new ConcurrentHashMap<>();
  private static final ThreadLocal<ResultFuture<?>> LOCAL = new ThreadLocal<>();

  private final Class<T> clazz;
  private final CompletableFuture<Response> future;

  @Setter
  private Consumer<T> onSuccess;
  @Setter
  private Consumer<Throwable> onFailure;

  public ResultFuture(Class<T> clazz) {
    this.clazz = clazz;
    this.future = new CompletableFuture<>();
  }

  /**
   * 添加future
   *
   * @param id 请求id
   * @param future CompletableFuture
   */
  public static void add(Long id, ResultFuture<?> future) {
    FUTURES.put(id, future);
  }

  /**
   * 完成CompletableFuture
   *
   * @param response 响应
   */
  public static void complete(Response response) {
    ResultFuture<?> future = FUTURES.get(response.getId());
    if (future != null) {
      try {
        future.completeFuture(response);
      } finally {
        remove(response.getId());
      }
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

  /**
   * 获取ThreadLocal中的future并清除
   *
   * @param <T> 返回结果类型
   * @return resultFuture
   */
  @SuppressWarnings("unchecked")
  public static <T> ResultFuture<T> future() {
    try {
      return (ResultFuture<T>) LOCAL.get();
    } finally {
      LOCAL.remove();
    }
  }

  /**
   * 保存到ThreadLocal中
   */
  public void local() {
    LOCAL.set(this);
  }

  /**
   * 获取结果
   *
   * @return result
   * @throws Throwable 当调用异常时抛出
   */
  public T get() throws Throwable {
    Response response = future.join();
    if (response.hasError()) {
      throw response.getException();
    } else {
      return clazz.cast(response.getResult());
    }
  }

  /**
   * 获取结果
   *
   * @param timeout 超时时间
   * @param unit 时间单位
   * @return result
   * @throws Throwable 当调用异常时抛出
   */
  public T get(long timeout, TimeUnit unit) throws Throwable {
    Response response = future.get(timeout, unit);
    if (response.hasError()) {
      throw response.getException();
    } else {
      return clazz.cast(response.getResult());
    }
  }

  private void completeFuture(Response response) {
    future.complete(response);
    if (onFailure != null && response.hasError()) {
      onFailure.accept(response.getException());
    }
    if (onSuccess != null && !response.hasError()) {
      onSuccess.accept(clazz.cast(response.getResult()));
    }
  }
}
