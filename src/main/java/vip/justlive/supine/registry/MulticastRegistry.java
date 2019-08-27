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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.ExpiringMap;
import vip.justlive.oxygen.core.util.SystemUtils;
import vip.justlive.oxygen.core.util.ThreadUtils;
import vip.justlive.supine.codec.Serializer;
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
@RequiredArgsConstructor
public class MulticastRegistry extends AbstractRegistry {

  private static final int BUFFER_SIZE = 64 * 1024;
  private static final long INTERVAL = 1_000;

  private final List<RequestKey> keys = new CopyOnWriteArrayList<>();
  private final ServiceConfig serviceConfig;
  private final ClientConfig clientConfig;
  private MulticastSocket socket;
  private DatagramPacket packet;
  private volatile boolean stopped;
  private ScheduledExecutorService schedule;

  private ExpiringMap<InetSocketAddress, List<RequestKey>> services;
  private Map<RequestKey, List<InetSocketAddress>> requestToService;


  public MulticastRegistry(ServiceConfig config) {
    this.serviceConfig = config;
    this.clientConfig = null;
  }

  public MulticastRegistry(ClientConfig config) {
    this.serviceConfig = null;
    this.clientConfig = config;
    this.services = ExpiringMap.<InetSocketAddress, List<RequestKey>>builder()
        .expiration(INTERVAL * 2, TimeUnit.MILLISECONDS).build();
    this.requestToService = new HashMap<>(2);
    super.init(config);
  }

  @Override
  public void register(List<RequestKey> keys) {
    this.keys.addAll(keys);
  }

  @Override
  public void unregister(List<RequestKey> keys) {
    this.keys.removeAll(keys);
  }

  @Override
  public void start() throws IOException {
    schedule = ThreadUtils.newScheduledExecutor(2, "registry-%d");
    if (serviceConfig != null) {
      service(serviceConfig);
    } else if (clientConfig != null) {
      client(clientConfig);
    }
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
    schedule.scheduleWithFixedDelay(this::refresh, INTERVAL, INTERVAL / 2, TimeUnit.MILLISECONDS);
  }

  private void service(ServiceConfig config) throws IOException {
    socket = new MulticastSocket();
    socket.setReuseAddress(true);
    socket.setTimeToLive(255);
    socket.setSendBufferSize(BUFFER_SIZE);

    RegistryInfo registryInfo = new RegistryInfo().setHost(config.getHost())
        .setPort(config.getPort()).setKeys(keys);
    byte[] data = Serializer.def().encode(registryInfo);
    String registryAddress = config.getRegistryAddress();
    if (registryAddress == null || registryAddress.trim().length() == 0) {
      registryAddress = RegistryInfo.DEFAULT_MULTICAST_ADDRESS;
    }
    packet = new DatagramPacket(data, data.length, SystemUtils.parseAddress(registryAddress));
    stopped = false;
    schedule.schedule(new Sender(), INTERVAL, TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    stopped = true;
    keys.clear();
    if (schedule != null) {
      schedule.shutdown();
    }
  }

  @Override
  public ClientTransport discovery(RequestKey key) {
    List<InetSocketAddress> addresses = requestToService.get(key);
    if (addresses == null) {
      throw Exceptions.fail("没有可用的服务提供者");
    }
    return load(addresses);
  }

  private void refresh() {
    if (services == null) {
      return;
    }
    Map<RequestKey, List<InetSocketAddress>> map = new HashMap<>(2);
    services.forEach(
        (k, v) -> v.forEach(item -> map.computeIfAbsent(item, rk -> new ArrayList<>()).add(k)));
    requestToService = map;
  }

  private class Sender implements Runnable {

    @Override
    public void run() {
      if (stopped || socket == null || packet == null) {
        return;
      }
      try {
        socket.send(packet);
      } catch (IOException e) {
        log.warn("multicast registry send failed", e);
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
      RegistryInfo info = (RegistryInfo) Serializer.def().decode(data);
      if (info.getKeys() == null || info.getKeys().isEmpty()) {
        return;
      }
      InetSocketAddress address = new InetSocketAddress(info.getHost(), info.getPort());
      services.put(address, info.getKeys());
    }
  }
}
