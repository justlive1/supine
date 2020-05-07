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

package vip.justlive.jmh.rpc.supine;

import java.io.IOException;
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
import vip.justlive.jmh.rpc.FooService;
import vip.justlive.jmh.rpc.Objs;
import vip.justlive.supine.client.ReferenceFactory;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.ResultFuture;

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
public class SupineBenchmark {

  private FooService fooService;
  private ReferenceFactory factory;

  @Setup
  public void setup() throws IOException {
    ClientConfig config = new ClientConfig();
    config.setTimeout(10000);
    config.setAsync(true);
    config.setRegistryAddress("localhost:10082");

    factory = new ReferenceFactory(config);
    fooService = factory.create(FooService.class);

    factory.start();
  }

  @TearDown
  public void tearDown() {
    factory.stop();
  }

  @Benchmark
  public void empty() throws Throwable {
    fooService.empty();
    ResultFuture.future().get();
  }

  @Benchmark
  public void str1k() throws Throwable {
    fooService.str(Objs.str1k);
    ResultFuture.future().get();
  }

  @Benchmark
  public void str10k() throws Throwable {
    fooService.str(Objs.str10k);
    ResultFuture.future().get();
  }

  @Benchmark
  public void obj() throws Throwable {
    fooService.obj(Objs.person);
    ResultFuture.future().get();
  }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(SupineBenchmark.class.getSimpleName()).build())
        .run();
  }
}
