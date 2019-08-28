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
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.supine.client.ReferenceFactory;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.registry.MulticastRegistry;
import vip.justlive.supine.service.ServiceFactory;

/**
 * @author wubo
 */
public class RpcTest {

  @Test
  public void testLocal() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    new Thread(() -> {
      localService(latch);
    }).start();
    latch.await();

    ClientConfig config = new ClientConfig();
    config.setIdleTimeout(120);
    config.setRegistryAddress("localhost:10086");
    ReferenceFactory factory = new ReferenceFactory(config);
    try {
      factory.start();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
    discover(factory);

    try {
      Thread.sleep(1300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    factory.stop();
  }

  @Test
  public void testMulticast() throws IOException {
    ClientConfig config = new ClientConfig();
    ReferenceFactory factory = new ReferenceFactory(config, new MulticastRegistry(config));
    factory.start();
    new Thread(this::multicastService).start();
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Assert.fail();
    }
    discover(factory);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Assert.fail();
    }
    factory.stop();
  }

  private void discover(ReferenceFactory factory) {
    Say say = factory.create(Say.class);

    String msg = "say";
    Assert.assertEquals(msg, say.hello(msg));

    System.out.println(say.hashCode());

    Say say2 = factory.create(Say.class, "1");

    try {
      say2.hello(msg);
      Assert.fail();
    } catch (CodedException e) {
      //ignore
    }

    say2 = factory.create(Say.class, "2");
    Assert.assertEquals("2:" + msg, say2.hello(msg));

  }

  private void registry(ServiceFactory factory) {
    factory.register(new SayImpl());
    factory.register(new SayImpl2(), "2");
    try {
      factory.start();
    } catch (IOException e) {
      Assert.fail();
    }
  }

  private void localService(CountDownLatch latch) {
    ServiceConfig config = new ServiceConfig("localhost", 10086);
    ServiceFactory factory = new ServiceFactory(config);
    registry(factory);
    latch.countDown();
    factory.sync();
  }

  private void multicastService() {
    ServiceConfig config = new ServiceConfig(10086);
    ServiceFactory factory = new ServiceFactory(config, new MulticastRegistry(config));
    registry(factory);
    new Thread(() -> {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      factory.stop();
    }).start();
    factory.sync();
  }
}
