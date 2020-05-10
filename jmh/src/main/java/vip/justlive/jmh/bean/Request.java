/*
 *  Copyright (C) 2020 justlive1
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

package vip.justlive.jmh.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wubo
 */
@Data
@Accessors(chain = true)
public class Request implements Serializable {

  private static final long serialVersionUID = 1773129302314578747L;

  private long id;
  private String name;
  private int sex;
  private String email;
  private String mobile;
  private String address;
  private String icon;
  private List<Integer> permissions;
  private int status;
  private Date createTime;
  private Date updateTime;
}
