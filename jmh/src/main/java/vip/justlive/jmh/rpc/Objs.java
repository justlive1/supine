/*
 * Copyright (C) 2020 the original author or authors.
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

package vip.justlive.jmh.rpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wubo
 */
public class Objs {

  public static final String str1k;
  public static final String str10k;
  public static final Person person;

  static {
    StringBuilder builder = new StringBuilder();
    int length = 1024;
    for (int i = 0; i < length; i++) {
      builder.append((char) (i % 128));
    }
    str1k = builder.toString();

    length *= 9;
    for (int i = 0; i < length; i++) {
      builder.append((char) (i % 128));
    }
    str10k = builder.toString();

    person = new Person();
    person.setName("jmh");
    person.setFullName(new FullName("first", "last"));
    person.setBirthday(new Date());
    List<String> phoneNumber = new ArrayList<>();
    phoneNumber.add("123");
    person.setPhoneNumber(phoneNumber);
    person.setEmail(phoneNumber);
    Map<String, String> address = new HashMap<>();
    address.put("hat", "123");
    person.setAddress(address);
  }
}
