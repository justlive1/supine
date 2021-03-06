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

package vip.justlive.jmh.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.service.Invoker;

/**
 * @author wubo
 */
public class CglibInvoker implements Invoker {

  private final Object target;
  private final FastMethod method;

  public CglibInvoker(Object target, Method method) {
    this.target = target;
    this.method = FastClass.create(target.getClass()).getMethod(method);

  }

  @Override
  public Object invoke(Object[] args) {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      throw Exceptions.wrap(e);
    }
  }
}
