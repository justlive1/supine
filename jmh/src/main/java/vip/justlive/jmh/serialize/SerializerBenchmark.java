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

package vip.justlive.jmh.serialize;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.nustaq.serialization.FSTConfiguration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * @author wubo
 */
@Fork(1)
@Threads(1)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 3, time = 3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class SerializerBenchmark {

  private static final Request REQUEST;
  private static final List<Request> LIST;
  private static final Gson GSON;
  private static final ObjectMapper OBJECT_MAPPER;
  private static final Kryo KRYO;
  private static final FSTConfiguration FST;

  static {
    REQUEST = new Request();
    REQUEST.setId(System.currentTimeMillis());
    REQUEST.setMethod("post");
    REQUEST.setUrl("http://localhost:8080/api");
    REQUEST.setBody("key=123&value=5674");
    REQUEST.setHeaders(MoreObjects.mapOf("x-id", "xet1", "token", "cg42145"));

    LIST = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      LIST.add(new Request().setId(i).setMethod("get").setUrl("http://localhost:1234/api")
          .setBody("xetx=2414124&sdgage=134").setHeaders(MoreObjects.mapOf("k", "1", "v", "@")));
    }

    GSON = new Gson();
    OBJECT_MAPPER = new ObjectMapper();
    KRYO = new Kryo();
    FST = FSTConfiguration.createDefaultConfiguration();
  }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(SerializerBenchmark.class.getSimpleName()).build())
        .run();
  }

  @TearDown
  public void dataSize() throws Exception {
    System.out.println("data size:");
    System.out.println("jdk      " + jdk().length);
    System.out.println("fastjson " + fastjson().length);
    System.out.println("fst      " + fst().length);
    System.out.println("jackson  " + jackson().length);
    System.out.println("kryo     " + kryo().length);
    System.out.println("gson     " + gson().length);
    System.out.println("hessian  " + hessian().length);
  }

  @Benchmark
  public byte[] jdk() throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(
        out)) {
      oos.writeObject(LIST);
      out.toByteArray();
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(
        out)) {
      oos.writeObject(REQUEST);
      return out.toByteArray();
    }
  }

  @Benchmark
  public byte[] fastjson() {
    JSON.toJSONBytes(LIST, WriteClassName);
    return JSON.toJSONBytes(REQUEST, WriteClassName);
  }

  @Benchmark
  public byte[] jackson() throws JsonProcessingException {
    OBJECT_MAPPER.writeValueAsBytes(LIST);
    return OBJECT_MAPPER.writeValueAsBytes(REQUEST);
  }

  @Benchmark
  public byte[] gson() {
    GSON.toJson(LIST).getBytes();
    return GSON.toJson(REQUEST).getBytes();
  }

  @Benchmark
  public byte[] kryo() throws IOException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream(); Output output = new Output(os)) {
      KRYO.writeObject(output, LIST);
      output.flush();
      os.toByteArray();
    }
    try (ByteArrayOutputStream os = new ByteArrayOutputStream(); Output output = new Output(os)) {
      KRYO.writeObject(output, REQUEST);
      output.flush();
      return os.toByteArray();
    }
  }

  @Benchmark
  public byte[] fst() {
    FST.asByteArray(LIST);
    return FST.asByteArray(REQUEST);
  }

  @Benchmark
  public byte[] hessian() throws Exception {
    ByteArrayOutputStream os1 = new ByteArrayOutputStream();
    Hessian2Output ho1 = new Hessian2Output(os1);
    try {
      ho1.writeObject(LIST);
      ho1.flush();
      os1.toByteArray();
    } finally {
      ho1.close();
      os1.close();
    }
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Hessian2Output ho = new Hessian2Output(os);
    try {
      ho.writeObject(REQUEST);
      ho.flush();
      return os.toByteArray();
    } finally {
      ho.close();
      os.close();
    }
  }

}
