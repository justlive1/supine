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

package vip.justlive.supine.service;

import java.io.IOException;
import org.junit.Test;
import vip.justlive.supine.SayImpl;
import vip.justlive.supine.protocol.ServiceConfig;

/**
 * @author wubo
 */
public class ServiceFactoryTest {

  @Test
  public void test() throws IOException {
    ServiceConfig config = new ServiceConfig("localhost", 10086);
    ServiceFactory factory = new ServiceFactory(config);
    factory.register(new SayImpl());
    factory.start();
  }
}