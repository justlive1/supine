package vip.justlive.supine.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.SystemUtils;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.Client;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;
import vip.justlive.oxygen.core.util.net.aio.LengthFrame;
import vip.justlive.oxygen.core.util.net.aio.Server;
import vip.justlive.supine.codec.KryoSerializer;
import vip.justlive.supine.codec.Serializer;
import vip.justlive.supine.common.ClientConfig;
import vip.justlive.supine.common.RequestKey;
import vip.justlive.supine.common.RequestKeyWrapper;
import vip.justlive.supine.common.ServiceConfig;
import vip.justlive.supine.service.ServiceMethodInvoker;
import vip.justlive.supine.transport.ClientTransport;
import vip.justlive.supine.transport.impl.ClientHandler;
import vip.justlive.supine.transport.impl.ReverseAioClientTransport;
import vip.justlive.supine.transport.impl.ReverseClientListener;
import vip.justlive.supine.transport.impl.ServerHandler;
import vip.justlive.supine.transport.impl.Transport;

/**
 * 反向注册中心，适用于客户端有对外ip，服务端不对外
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class ReverseRegistry implements Registry {

  private final ServiceConfig serviceConfig;
  private final ClientConfig clientConfig;
  private final Serializer serializer;
  private final List<ReverseAioClientTransport> transports = new ArrayList<>();
  private Client client;
  private Server server;


  public ReverseRegistry(ServiceConfig serviceConfig) {
    this.serviceConfig = serviceConfig;
    this.clientConfig = null;
    this.serializer = MoreObjects.firstNonNull(serviceConfig.getSerializer(),
        KryoSerializer.INSTANCE);
  }

  public ReverseRegistry(ClientConfig clientConfig) {
    this.serviceConfig = null;
    this.clientConfig = clientConfig;
    this.serializer = MoreObjects.firstNonNull(clientConfig.getSerializer(),
        KryoSerializer.INSTANCE);
  }

  @Override
  public void register(List<RequestKey> keys) {

  }

  @Override
  public void unregister(List<RequestKey> keys) {

  }

  @Override
  public void start() throws IOException {
    if (serviceConfig != null) {
      service(serviceConfig);
    } else if (clientConfig != null) {
      client(clientConfig);
    }
  }

  @Override
  public void stop() {
    if (serviceConfig != null) {
      client.close();
    } else if (clientConfig != null) {
      server.stop();
    }
  }

  @Override
  public ClientTransport discovery(RequestKey key) {
    int size = transports.size();
    int index = AbstractRegistry.RANDOM.nextInt(size);
    for (int i = 0; i < size; i++) {
      ClientTransport transport = transports.get(index);
      index = (index + 1) % size;
      if (transport != null && !transport.isClosed() && transport.lookup(key) != null) {
        return transport;
      }
    }
    throw Exceptions.fail("远程服务不可用");
  }

  private void client(ClientConfig clientConfig) throws IOException {
    log.info("client start a reverse server {}", clientConfig.getRegistryAddress());
    GroupContext groupContext = new GroupContext(new ClientHandler(serializer));
    groupContext.setAioListener(new ReverseClientListener(serializer, transports));
    groupContext.setDaemon(true);
    server = new Server(groupContext);
    server.start(SystemUtils.parseAddress(clientConfig.getRegistryAddress()));
  }

  private void service(ServiceConfig serviceConfig) throws IOException {
    log.info("server connect to reverse client registry server {}",
        serviceConfig.getRegistryAddress());
    GroupContext groupContext = new GroupContext(new ServerHandler(serializer));
    groupContext.setRetryEnabled(true);
    groupContext.setDaemon(true);
    client = new Client(groupContext);
    for (String address : serviceConfig.getRegistryAddress().trim().split(Strings.COMMA)) {
      ChannelContext channel = client.connect(SystemUtils.parseAddress(address));
      channel.join();
      RequestKeyWrapper requestKeyWrapper = new RequestKeyWrapper(
          ServiceMethodInvoker.requestKeys());
      log.info("server send endpoint to client {}", requestKeyWrapper);
      channel.write(new LengthFrame().setType(Transport.ENDPOINT)
          .setBody(serializer.encode(requestKeyWrapper)));
    }
  }

}
