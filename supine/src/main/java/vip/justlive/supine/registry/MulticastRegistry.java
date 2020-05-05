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

package vip.justlive.supine.registry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.core.util.MoreObjects;
import vip.justlive.oxygen.core.util.SystemUtils;
import vip.justlive.oxygen.core.util.ThreadUtils;
import vip.justlive.supine.codec.KryoSerializer;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.RegistryInfo;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.transport.ClientTransport;

/**
 * Multicast注册实现
 *
 * @author wubo
 */
@Slf4j
public class MulticastRegistry extends AbstractRegistry {

  private static final int BUFFER_SIZE = 64 * 1024;
  private static final long INTERVAL = 10_000;

  private final RegistryInfo registryInfo = new RegistryInfo().setKeys(new LinkedList<>());
  private final ServiceConfig serviceConfig;
  private final ClientConfig clientConfig;
  private MulticastSocket socket;
  private DatagramPacket packet;
  private volatile boolean stopped;
  private ScheduledExecutorService schedule;

  private ExpiringMap<InetSocketAddress, List<RequestKey>> services;
  private Map<RequestKey, List<InetSocketAddress>> requestToService;
  private long lastUpdated;
  private InetSocketAddress registryAddress;

  public MulticastRegistry(ServiceConfig config) {
    this.serviceConfig = config;
    this.clientConfig = null;
    this.serializer = MoreObjects.firstNonNull(config.getSerializer(), KryoSerializer.INSTANCE);
  }

  public MulticastRegistry(ClientConfig config) {
    this.serviceConfig = null;
    this.clientConfig = config;
    this.serializer = MoreObjects.firstNonNull(config.getSerializer(), KryoSerializer.INSTANCE);
    this.services = ExpiringMap.<InetSocketAddress, List<RequestKey>>builder()
        .expiration(INTERVAL * 2, TimeUnit.MILLISECONDS).build();
    this.requestToService = new HashMap<>(2);
    super.init(config);
  }

  @Override
  public void register(List<RequestKey> keys) {
    this.registryInfo.getKeys().addAll(keys);
    if (packet != null) {
      byte[] data = serializer.encode(registryInfo);
      packet = new DatagramPacket(data, data.length, registryAddress);
    }
  }

  @Override
  public void unregister(List<RequestKey> keys) {
    this.registryInfo.getKeys().removeAll(keys);
    if (packet != null) {
      byte[] data = serializer.encode(registryInfo);
      packet = new DatagramPacket(data, data.length, registryAddress);
    }
  }

  @Override
  public void start() throws IOException {
    super.start();
    schedule = ThreadUtils.newScheduledExecutor(1, "registry");
    if (serviceConfig != null) {
      service(serviceConfig);
    } else if (clientConfig != null) {
      client(clientConfig);
    }
  }

  @Override
  public void stop() {
    stopped = true;
    registryInfo.getKeys().clear();
    if (schedule != null) {
      schedule.shutdown();
    }
    super.stop();
  }

  @Override
  public ClientTransport discovery(RequestKey key) {
    List<InetSocketAddress> addresses = requestToService.get(key);
    if (addresses == null) {
      throw Exceptions.fail("没有可用的服务提供者");
    }
    return load(addresses, key);
  }

  private void client(ClientConfig clientConfig) throws IOException {
    String address = clientConfig.getRegistryAddress();
    if (address == null || address.trim().length() == 0) {
      address = RegistryInfo.DEFAULT_MULTICAST_ADDRESS;
    }
    InetSocketAddress bindAddress = SystemUtils.parseAddress(address);
    socket = new MulticastSocket(bindAddress.getPort());
    socket.setReuseAddress(true);
    socket.setReceiveBufferSize(BUFFER_SIZE);
    socket.setSoTimeout((int) INTERVAL);
    socket.joinGroup(bindAddress.getAddress());

    packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
    stopped = false;
    schedule.execute(new Receiver());
  }

  private void service(ServiceConfig config) throws IOException {
    socket = new MulticastSocket();
    socket.setReuseAddress(true);
    socket.setTimeToLive(255);
    socket.setSendBufferSize(BUFFER_SIZE);

    registryInfo.setHost(config.getHost()).setPort(config.getPort());
    if (config.getRegistryAddress() == null || config.getRegistryAddress().trim().length() == 0) {
      registryAddress = SystemUtils.parseAddress(RegistryInfo.DEFAULT_MULTICAST_ADDRESS);
    } else {
      registryAddress = SystemUtils.parseAddress(config.getRegistryAddress());
    }

    byte[] data = serializer.encode(registryInfo);
    packet = new DatagramPacket(data, data.length, registryAddress);

    stopped = false;
    schedule.execute(new Sender());
  }

  private class Sender implements Runnable {

    @Override
    public void run() {
      if (serviceConfig == null || stopped || socket == null || packet == null) {
        return;
      }
      if (log.isDebugEnabled()) {
        log.info("[{}]开始注册服务，[{}]个调用方法", registryAddress, registryInfo.getKeys().size());
      }
      try {
        socket.send(packet);
      } catch (IOException e) {
        log.warn("multicast注册消息发送失败", e);
      }
      if (schedule != null) {
        schedule.schedule(this, INTERVAL, TimeUnit.MILLISECONDS);
      }
    }
  }

  private class Receiver implements Runnable {

    @Override
    public void run() {
      while (!stopped) {
        try {
          socket.receive(packet);
          handleReceive();
        } catch (IOException e) {
          // ignore
        }
      }
    }

    private void handleReceive() {
      if (packet == null) {
        return;
      }
      final byte[] data = packet.getData();
      RegistryInfo info = (RegistryInfo) serializer.decode(data);

      if (log.isDebugEnabled()) {
        log.debug("注册中心获取到一个服务地址 -> [{}:{}]", info.getHost(), info.getPort());
      }

      if (info.getKeys() == null || info.getKeys().isEmpty()) {
        return;
      }
      InetSocketAddress address = new InetSocketAddress(info.getHost(), info.getPort());
      services.put(address, info.getKeys());

      long curr = System.currentTimeMillis();
      if (curr - lastUpdated < INTERVAL) {
        return;
      }

      lastUpdated = curr;
      Map<RequestKey, List<InetSocketAddress>> map = new HashMap<>(2);
      services.forEach(
          (k, v) -> v.forEach(item -> map.computeIfAbsent(item, rk -> new ArrayList<>()).add(k)));
      requestToService = map;
    }
  }
}
