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

package vip.justlive.supine.spring.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.client.Reference;
import vip.justlive.supine.client.ReferenceFactory;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.registry.Registry;
import vip.justlive.supine.spring.EnableRpc;

/**
 * Reference注解注入
 *
 * @author wubo
 */
@Slf4j
public class ReferenceBeanPostProcessor implements BeanPostProcessor, EnvironmentAware,
    BeanFactoryAware, DisposableBean {

  private Environment environment;
  private BeanFactory beanFactory;

  private ReferenceFactory factory;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    ReflectionUtils.doWithFields(bean.getClass(), field -> fieldCallback(field, bean));
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return bean;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  private void fieldCallback(Field field, Object bean) {
    Reference reference = AnnotationUtils.getAnnotation(field, Reference.class);
    if (reference == null || Modifier.isStatic(field.getModifiers()) || !field.getType()
        .isInterface()) {
      return;
    }

    if (factory == null) {
      factory = initFactory();
    }

    Object proxy = factory.create(field.getType(), reference.version());
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, bean, proxy);
  }

  private ReferenceFactory initFactory() {
    ClientConfig config = new ClientConfig();

    String timeout = environment.getProperty(EnableRpc.TIMEOUT);
    if (StringUtils.hasText(timeout)) {
      config.setTimeout(Integer.parseInt(timeout));
    }
    String idleTimeout = environment.getProperty(EnableRpc.CLIENT_IDLE_TIMEOUT);
    if (StringUtils.hasText(idleTimeout)) {
      config.setIdleTimeout(Integer.parseInt(idleTimeout));
    }
    String async = environment.getProperty(EnableRpc.CLIENT_ASYNC);
    if (StringUtils.hasText(async)) {
      config.setAsync(Boolean.parseBoolean(async));
    }
    String registryAddress = environment.getProperty(EnableRpc.REGISTRY_ADDRESS);
    if (StringUtils.hasText(registryAddress)) {
      config.setRegistryAddress(registryAddress);
    }

    try {
      Registry registry = beanFactory.getBean(Registry.class);
      factory = new ReferenceFactory(config, registry);
    } catch (NoSuchBeanDefinitionException e) {
      String registryType = environment.getProperty(EnableRpc.REGISTRY_TYPE);
      if (StringUtils.hasText(registryType)) {
        config.setRegistryType(Integer.parseInt(registryType));
      }
      factory = new ReferenceFactory(config);
    }

    if (log.isDebugEnabled()) {
      log.debug("启动Rpc客户端，注册中心[{}|{}]", config.getRegistryType(), config.getRegistryAddress());
    }
    try {
      factory.start();
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
    return factory;
  }

  @Override
  public void destroy() {
    if (factory != null) {
      if (log.isDebugEnabled()) {
        log.debug("关闭Rpc客户端");
      }
      factory.stop();
      factory = null;
    }
  }
}
