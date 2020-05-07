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

package vip.justlive.supine.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * Javassist
 *
 * @author wubo
 */
public class JavassistInvoker implements Invoker {

  private static final String CLASS_NAME = "vip.justlive.supine.service.Invoker_%s_%s";
  private static final AtomicInteger COUNT = new AtomicInteger();

  private final Invoker invoker;

  public JavassistInvoker(Object target, Method method) {
    try {
      this.invoker = createClass(target, method);
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Object invoke(Object[] args) {
    return invoker.invoke(args);
  }

  private Invoker createClass(Object target, Method method)
      throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    String className = String.format(CLASS_NAME, method.getName(), COUNT.getAndIncrement());
    ClassPool pool = ClassPool.getDefault();
    CtClass invokerClass = pool.makeClass(className);
    invokerClass.setInterfaces(new CtClass[]{pool.getCtClass(Invoker.class.getName())});

    CtField targetField = new CtField(pool.get(target.getClass().getName()), "target",
        invokerClass);
    targetField.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
    invokerClass.addField(targetField);

    CtConstructor constructor = new CtConstructor(
        new CtClass[]{pool.get(target.getClass().getName())}, invokerClass);
    constructor.setBody("{$0.target = $1;}");
    invokerClass.addConstructor(constructor);

    StringBuilder invokeMethod = new StringBuilder();
    invokeMethod.append("public Object invoke(Object[] args) {\r\n");

    StringBuilder result = new StringBuilder("target.").append(method.getName()).append("(");
    Class<?>[] params = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      param(result, i, params[i]);
      if (i < params.length - 1) {
        result.append(",");
      }
    }
    result.append(")");
    result(invokeMethod, result, method.getReturnType());
    invokeMethod.append(";\r\n}");
    invokerClass.addMethod(CtNewMethod.make(invokeMethod.toString(), invokerClass));
    return (Invoker) invokerClass.toClass().getConstructor(target.getClass()).newInstance(target);
  }

  private void param(StringBuilder result, int index, Class<?> type) {
    result.append("((").append(ClassUtils.wrap(type).getName()).append(")").append("args[")
        .append(index).append("])");
    if (ClassUtils.isPrimitive(type)) {
      result.append(".").append(type.getName()).append("Value()");
    }
  }

  private void result(StringBuilder invokeMethod, StringBuilder result, Class<?> type) {
    String str = result.toString();
    if (ClassUtils.isPrimitive(type) && type != void.class) {
      invokeMethod.append("return ").append(ClassUtils.wrap(type).getName()).append(".valueOf(")
          .append(str).append(")");
    } else if (type == void.class) {
      invokeMethod.append(str).append(";\r\nreturn null");
    } else {
      invokeMethod.append("return ").append(str);
    }
  }

}
