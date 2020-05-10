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

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import vip.justlive.jmh.bean.Request;

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

  private static Object OBJ;
  private static final ObjectMapper OBJECT_MAPPER;
  private static final Gson GSON;
  private static final Kryo KRYO;
  private static final FSTConfiguration FST;

  static {

    List<Request> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      list.add(build());
    }

    OBJ = build();
    OBJ = list;
    GSON = new Gson();
    OBJECT_MAPPER = new ObjectMapper();
    KRYO = new Kryo();
    KRYO.setReferences(false);
    FST = FSTConfiguration.createDefaultConfiguration();
  }

  public static void main(String[] args) throws RunnerException {
    new Runner(new OptionsBuilder().include(SerializerBenchmark.class.getSimpleName()).build())
        .run();
  }

  static Request build() {
    Request request = new Request();
    request.setId(System.currentTimeMillis());
    request.setName("Jmh");
    request.setSex(1);
    request.setEmail("jmh@gmail.com");
    request.setMobile("1899999999");
    request.setAddress("广州市天河区猎德");
    request.setIcon("https://www.baidu.com/img/bd_logo1.png");
    request.setStatus(1);
    request.setCreateTime(new Date());
    request.setUpdateTime(request.getCreateTime());
    request.setPermissions(new ArrayList<>(
        Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 19, 88, 86, 89, 90, 91, 92)));
    return request;
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
  public byte[] jdk() throws Exception {
    byte[] bytes;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(
        out)) {
      oos.writeObject(OBJ);
      bytes = out.toByteArray();
    }
    try (ByteArrayInputStream in = new ByteArrayInputStream(
        bytes); ObjectInputStream oin = new ObjectInputStream(in)) {
      oin.readObject();
    }
    return bytes;
  }

  @Benchmark
  public byte[] fastjson() {
    byte[] bytes = JSON.toJSONBytes(OBJ);
    JSON.parse(bytes);
    return bytes;
  }

  @Benchmark
  public byte[] jackson() throws IOException {
    byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(OBJ);
    OBJECT_MAPPER.readValue(bytes, OBJ.getClass());
    return bytes;
  }

  @Benchmark
  public byte[] gson() {
    byte[] bytes = GSON.toJson(OBJ).getBytes();
    GSON.fromJson(new String(bytes), OBJ.getClass());
    return bytes;
  }

  @Benchmark
  public byte[] kryo() {
    byte[] bytes;
    try (FastOutput output = new FastOutput(64, -1)) {
      KRYO.writeObject(output, OBJ);
      bytes = output.toBytes();
    }
    try (FastInput input = new FastInput(bytes)) {
      KRYO.readObject(input, OBJ.getClass());
    }
    return bytes;
  }

  @Benchmark
  public byte[] fst() {
    byte[] bytes = FST.asByteArray(OBJ);
    FST.asObject(bytes);
    return bytes;
  }

  @Benchmark
  public byte[] hessian() throws Exception {
    byte[] bytes;
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Hessian2Output ho = new Hessian2Output(os);
    try {
      ho.writeObject(OBJ);
      ho.flush();
      bytes = os.toByteArray();
    } finally {
      ho.close();
      os.close();
    }

    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    Hessian2Input hi = new Hessian2Input(in);
    try {
      hi.readObject();
    } finally {
      hi.close();
      in.close();
    }
    return bytes;
  }

}
