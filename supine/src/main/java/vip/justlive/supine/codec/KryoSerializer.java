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

package vip.justlive.supine.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.io.FastOutput;
import com.esotericsoftware.kryo.pool.KryoPool;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * kryo序列化实现
 *
 * @author wubo
 */
public class KryoSerializer implements Serializer {

  public static final KryoSerializer INSTANCE = new KryoSerializer();

  private final KryoPool pool = new KryoPool.Builder(this::create).softReferences().build();

  private Kryo create() {
    Kryo kryo = new Kryo();
    kryo.setReferences(true);
    kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
    kryo.setRegistrationRequired(false);
    UnmodifiableCollectionsSerializer.registerSerializers(kryo);
    return kryo;
  }

  @Override
  public byte[] encode(Object obj) {
    return pool.run(kryo -> {
      try (FastOutput output = new FastOutput(64, -1)) {
        kryo.writeClassAndObject(output, obj);
        return output.toBytes();
      }
    });
  }


  @Override
  public Object decode(byte[] bytes) {
    return pool.run(kryo -> {
      try (FastInput input = new FastInput(bytes)) {
        return kryo.readClassAndObject(input);
      }
    });
  }
}
