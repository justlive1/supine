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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @author wubo
 */
@Data
public class Person implements Serializable {

  private static final long serialVersionUID = 5066728589421094583L;
  private String name;
  private FullName fullName;
  private Date birthday;
  private List<String> phoneNumber;
  private List<String> email;
  private Map<String, String> address;
  private List<Person> friends;

}
