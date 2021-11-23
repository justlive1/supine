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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vip.justlive.oxygen.core.exception.CodedException;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.supine.client.ReferenceFactory;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.ResultFuture;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.registry.MulticastRegistry;
import vip.justlive.supine.service.ServiceFactory;

/**
 * @author wubo
 */
class RpcTest {
  
  @Test
  void testLocal() {
    ServiceConfig serviceConfig = new ServiceConfig("localhost", 10086);
    ServiceFactory serviceFactory = new ServiceFactory(serviceConfig);
    registry(serviceFactory);
    ClientConfig config = new ClientConfig();
    config.setIdleTimeout(120);
    config.setRegistryAddress("localhost:10086");
    ReferenceFactory factory = new ReferenceFactory(config);
    try {
      factory.start();
    } catch (IOException e) {
      e.printStackTrace();
      Assertions.fail();
    }
    
    Say say = factory.create(Say.class);
    
    String msg = "say";
    Assertions.assertEquals(msg, say.hello(msg));
    
    Assertions.assertEquals(-12, say.test0(12));
    Assertions.assertEquals(new Integer(12), say.test1(12));
    Assertions.assertEquals(3L, say.test3());
    say.test2(3L, null);
    
    System.out.println(say.hashCode());
    
    Say say2 = factory.create(Say.class, "1");
    
    try {
      say2.hello(msg);
      Assertions.fail();
    } catch (CodedException e) {
      //ignore
      System.out.println(e);
    }
    
    say2 = factory.create(Say.class, "2");
    Assertions.assertEquals("2:" + msg, say2.hello(msg));
    
    ThreadUtils.sleep(1300);
    factory.stop();
  }
  
  //    @Test
  void testMulticast() throws Throwable {
    ClientConfig config = new ClientConfig();
    config.setAsync(true);
    ReferenceFactory factory = new ReferenceFactory(config, new MulticastRegistry(config));
    factory.start();
    
    ServiceConfig serviceConfig = new ServiceConfig(10086);
    ServiceFactory serviceFactory = new ServiceFactory(serviceConfig,
        new MulticastRegistry(serviceConfig));
    registry(serviceFactory);
    
    new Thread(() -> {
      ThreadUtils.sleep(3000);
      factory.stop();
    }).start();
    
    ThreadUtils.sleep(2000);
    
    Say say = factory.create(Say.class);
    
    String msg = "say";
    say.hello(msg);
    ResultFuture<String> future = ResultFuture.future();
    future.setOnSuccess(System.out::println);
    
    Assertions.assertEquals(msg, future.get());
    
    System.out.println(say.hashCode());
    
    Say say2 = factory.create(Say.class, "1");
    
    try {
      say2.hello(msg);
      Assertions.fail();
    } catch (CodedException e) {
      //ignore
    }
    
    say2 = factory.create(Say.class, "2");
    say2.hello(msg);
    future = ResultFuture.future();
    Assertions.assertEquals("2:" + msg, future.get());
    
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Assertions.fail();
    }
    factory.stop();
  }
  
  private void registry(ServiceFactory factory) {
    factory.register(new SayImpl());
    factory.register(new SayImpl2(), "2");
    try {
      factory.start();
    } catch (IOException e) {
      Assertions.fail();
    }
  }
  
}
