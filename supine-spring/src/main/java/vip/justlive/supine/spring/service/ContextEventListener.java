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

package vip.justlive.supine.spring.service;

import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.registry.Registry;
import vip.justlive.supine.service.Service;
import vip.justlive.supine.service.ServiceFactory;
import vip.justlive.supine.spring.EnableRpc;

/**
 * 容器事件监听
 *
 * @author wubo
 */
@Slf4j
public class ContextEventListener implements ApplicationListener<ApplicationContextEvent> {

  private ServiceFactory factory;

  @Override
  public void onApplicationEvent(ApplicationContextEvent event) {
    if (event instanceof ContextStartedEvent || event instanceof ContextRefreshedEvent) {
      stopRpc();
      startRpc(event);
    } else if (event instanceof ContextClosedEvent) {
      stopRpc();
    }
  }

  private void stopRpc() {
    if (factory == null) {
      return;
    }
    if (log.isDebugEnabled()) {
      log.debug("关闭Rpc服务");
    }
    factory.stop();
  }

  private void startRpc(ApplicationContextEvent event) {
    Map<String, Object> beans = event.getApplicationContext().getBeansWithAnnotation(Service.class);
    if (beans.isEmpty()) {
      log.warn("没有找到@{}注解的实例", Service.class.getName());
      return;
    }

    Environment env = event.getApplicationContext().getEnvironment();
    String host = env.getProperty(EnableRpc.SERVER_HOST);
    int port = Integer.parseInt(env.getProperty(EnableRpc.SERVER_PORT, "80"));

    ServiceConfig config;
    if (StringUtils.hasText(host)) {
      config = new ServiceConfig(host, port);
    } else {
      config = new ServiceConfig(port);
    }

    String timeout = env.getProperty(EnableRpc.TIMEOUT);
    if (StringUtils.hasText(timeout)) {
      config.setTimeout(Integer.parseInt(timeout));
    }

    Map<String, Registry> map = event.getApplicationContext().getBeansOfType(Registry.class);
    if (!map.isEmpty()) {
      factory = new ServiceFactory(config, map.values().iterator().next());
    } else {
      String registryType = env.getProperty(EnableRpc.REGISTRY_TYPE);
      if (StringUtils.hasText(registryType)) {
        config.setRegistryType(Integer.parseInt(registryType));
      }
      String registryAddress = env.getProperty(EnableRpc.REGISTRY_ADDRESS);
      if (StringUtils.hasText(registryType)) {
        config.setRegistryAddress(registryAddress);
      }
      factory = new ServiceFactory(config);
    }

    beans.forEach((k, v) -> factory.register(v));

    if (log.isDebugEnabled()) {
      log.debug("启动Rpc服务[{}:{}]，注册中心[{}|{}]", config.getHost(), config.getPort(),
          config.getRegistryType(), config.getRegistryAddress());
    }

    try {
      factory.start();
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }

  }
}
