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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.service.Invoker;

/**
 * @author wubo
 */
public class MethodHandleInvoker implements Invoker {

  private final Object target;
  private final MethodHandle methodHandle;

  public MethodHandleInvoker(Object target, Method method) {
    this.target = target;
    try {
      methodHandle = MethodHandles.lookup().findVirtual(target.getClass(), method.getName(),
          MethodType.methodType(method.getReturnType(), method.getParameterTypes())).bindTo(target);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Object invoke(Object[] args) {
    int len = args == null ? 0 : args.length;
    try {
      switch (len) {
        case 0:
          return methodHandle.invoke();
        case 1:
          return methodHandle.invoke(args[0]);
        case 2:
          return methodHandle.invoke(args[0], args[1]);
        case 3:
          return methodHandle.invoke(args[0], args[1], args[2]);
        case 4:
          return methodHandle.invoke(args[0], args[1], args[2], args[3]);
        case 5:
          return methodHandle.invoke(args[0], args[1], args[2], args[3], args[4]);
        default:
          return methodHandle.invokeWithArguments(args);
      }
    } catch (Throwable throwable) {
      throw Exceptions.wrap(throwable);
    }
  }

}
