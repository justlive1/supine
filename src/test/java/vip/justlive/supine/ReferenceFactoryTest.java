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

package vip.justlive.supine;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.supine.client.ReferenceFactory;
import vip.justlive.supine.common.ClientConfig;

/**
 * @author wubo
 */
public class ReferenceFactoryTest {

  private ReferenceFactory factory;
  private Say say;

  @Test
  public void test() throws IOException {

    ClientConfig config = new ClientConfig();
    config.setRegistryAddress("localhost:10086");
    factory = new ReferenceFactory(config);
    say = factory.create(Say.class);
    factory.start();

    String msg = "say";
    Assert.assertEquals(msg, say.hello(msg));

    Say say2 = factory.create(Say.class, "1");

    try {
      say2.hello(msg);
      Assert.fail();
    } catch (CodedException e) {
      //ignore
    }

    say2 = factory.create(Say.class, "2");
    Assert.assertEquals("2:" + msg, say2.hello(msg));

    factory.stop();
  }

}