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

package vip.justlive.jmh.rpc.motan;

import java.util.concurrent.TimeUnit;
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
import vip.justlive.jmh.bean.Objs;
import vip.justlive.jmh.rpc.FooServiceAsync;

/**
 * @author wubo
 */
@Fork(1)
@Threads(2)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MotanBenchmark {

  private ClassPathXmlApplicationContext client;
  private ClassPathXmlApplicationContext server;
  private FooServiceAsync fooService;

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(MotanBenchmark.class.getSimpleName()).build()).run();
  }

  @Setup
  public void setup() {
    server = new ClassPathXmlApplicationContext("classpath*:motan-server.xml");
    server.start();
    client = new ClassPathXmlApplicationContext("classpath*:motan-client.xml");
    client.start();
    fooService = client.getBean(FooServiceAsync.class);
  }

  @TearDown
  public void tearDown() {
    client.destroy();
    server.close();
  }

  @Benchmark
  public void empty() {
    fooService.emptyAsync()
            .getValue()
    ;
  }

  @Benchmark
  public void str1k() {
    fooService.strAsync(Objs.str1k)
            .getValue()
    ;
  }

  @Benchmark
  public void str10k() {
    fooService.strAsync(Objs.str10k)
            .getValue()
    ;
  }

  @Benchmark
  public void obj() {
    fooService.objAsync(Objs.person)
            .getValue()
    ;
  }
}
