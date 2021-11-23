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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * kryo序列化实现
 *
 * @author wubo
 */
public class KryoSerializer implements Serializer {
  
  public static final KryoSerializer INSTANCE = new KryoSerializer();
  
  private final Pool<Kryo> pool = new Pool<Kryo>(true, true, 16) {
    @Override
    protected Kryo create() {
      Kryo kryo = new Kryo();
      kryo.setReferences(true);
      kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
      kryo.setRegistrationRequired(false);
      UnmodifiableCollectionsSerializer.registerSerializers(kryo);
      SynchronizedCollectionsSerializer.registerSerializers(kryo);
      return kryo;
    }
  };
  
  @Override
  public byte[] encode(Object obj) {
    Kryo kryo = pool.obtain();
    try (Output output = new Output(64, -1)) {
      kryo.writeClassAndObject(output, obj);
      return output.toBytes();
    } finally {
      pool.free(kryo);
    }
  }
  
  
  @Override
  public Object decode(byte[] bytes) {
    Kryo kryo = pool.obtain();
    try (Input input = new Input(bytes)) {
      return kryo.readClassAndObject(input);
    } finally {
      pool.free(kryo);
    }
  }
}
