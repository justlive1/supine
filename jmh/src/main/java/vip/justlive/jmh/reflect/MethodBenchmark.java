/*
 *  Copyright (C) 2020 justlive1
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

package vip.justlive.jmh.reflect;

import java.lang.reflect.Method;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import vip.justlive.jmh.bean.MethodBean;
import vip.justlive.jmh.bean.Objs;
import vip.justlive.jmh.bean.Person;
import vip.justlive.supine.service.JavassistInvoker;

/**
 * @author wubo
 */
@Fork(1)
@Threads(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class MethodBenchmark {

  private Person input;
  private MethodBean bean;
  private ReflectInvoker reflect;
  private CglibInvoker cglib;
  private JavassistInvoker javassist;
  private MethodHandleInvoker methodHandle;

  public static void main(String[] args) throws Exception {
    new Runner(new OptionsBuilder().include(MethodBenchmark.class.getSimpleName()).build()).run();
  }


  @Setup
  public void setup() throws NoSuchMethodException {
    input = Objs.person;
    bean = new MethodBean();
    Method method = bean.getClass().getMethod("test", Person.class);
    reflect = new ReflectInvoker(bean, method);
    cglib = new CglibInvoker(bean, method);
    javassist = new JavassistInvoker(bean, method);
    methodHandle = new MethodHandleInvoker(bean, method);
  }

  @Benchmark
  public void direct() {
    bean.test(input);
  }

  @Benchmark
  public void reflect() {
    reflect.invoke(new Object[]{input});
  }

  @Benchmark
  public void cglib() {
    cglib.invoke(new Object[]{input});
  }

  @Benchmark
  public void javassist() {
    javassist.invoke(new Object[]{input});
  }

  @Benchmark
  public void methodHandle() {
    methodHandle.invoke(new Object[]{input});
  }

}
