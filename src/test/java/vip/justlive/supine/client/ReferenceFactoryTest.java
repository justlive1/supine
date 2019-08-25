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

package vip.justlive.supine.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vip.justlive.oxygen.core.domain.RespVo;
import vip.justlive.oxygen.core.util.ExecutorPool;
import vip.justlive.supine.Say;
import vip.justlive.supine.Type;
import vip.justlive.supine.protocol.ClientConfig;

/**
 * @author wubo
 */
public class ReferenceFactoryTest {

  private ReferenceFactory factory;
  private Say say;

  @Before
  public void before() {
    ClientConfig config = new ClientConfig();
    config.setRegistryAddress("localhost:10086");
    factory = new ReferenceFactory(config);
    say = factory.create(Say.class);
    factory.start();
  }

  @After
  public void after() {
    factory.stop();
  }

  @Test
  public void test() {
    ExecutorPool<?> pool = new ExecutorPool<>();

    for (int i = 0; i < 10000; i++) {
      pool.submit(this::sayHello);
    }

    pool.waitForAll();
  }

  private void sayHello() {
    String msg = Thread.currentThread().getName();
    RespVo<String> rsp = say.hello(msg);
    System.out.println(rsp);
    Assert.assertEquals(msg, rsp.getData());

    int code = say.echo(new Type().setCode(2));
    Assert.assertEquals(2, code);
  }

}
