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

package vip.justlive.supine.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * kryo序列化实现
 *
 * @author wubo
 */
public class KryoSerializer implements Serializer {

  static final KryoSerializer INSTANCE = new KryoSerializer();

  private final ThreadLocal<Kryo> kryoLocal = ThreadLocal.withInitial(this::create);

  private Kryo create() {
    Kryo kryo = new Kryo();
    kryo.setReferences(true);
    kryo.setRegistrationRequired(false);
    return kryo;
  }

  @Override
  public byte[] encode(Object obj) {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream(); Output output = new Output(os)) {
      kryoLocal.get().writeClassAndObject(output, obj);
      output.flush();
      return os.toByteArray();
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Object decode(byte[] bytes) {
    try (ByteArrayInputStream is = new ByteArrayInputStream(bytes); Input input = new Input(is)) {
      return kryoLocal.get().readClassAndObject(input);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
