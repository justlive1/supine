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

package vip.justlive.jmh.rpc.dubbo;

import java.util.concurrent.TimeUnit;
import org.apache.dubbo.rpc.RpcContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import vip.justlive.jmh.rpc.FooService;
import vip.justlive.jmh.rpc.Objs;

/**
 * @author wubo
 */
@Fork(1)
@Threads(4)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DubboBenchmark {

  private ClassPathXmlApplicationContext context;
  private FooService fooService;

  @Setup
  public void setup() {
    context = new ClassPathXmlApplicationContext("classpath*:dubbo-client.xml");
    context.start();
    fooService = context.getBean(FooService.class);
  }

  @TearDown
  public void tearDown() {
    context.destroy();
  }

  @Benchmark
  public void empty() throws Exception {
    fooService.empty();
    RpcContext.getContext().getFuture().get(15, TimeUnit.SECONDS);
  }

  @Benchmark
  public void str1k() throws Exception {
    fooService.str(Objs.str1k);
    RpcContext.getContext().getFuture().get(15, TimeUnit.SECONDS);
  }

  @Benchmark
  public void str10k() throws Exception {
    fooService.str(Objs.str10k);
    RpcContext.getContext().getFuture().get(15, TimeUnit.SECONDS);
  }

  @Benchmark
  public void obj() throws Exception {
    fooService.obj(Objs.person);
    RpcContext.getContext().getFuture().get(15, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(DubboBenchmark.class.getSimpleName()).build())
        .run();
  }
}
