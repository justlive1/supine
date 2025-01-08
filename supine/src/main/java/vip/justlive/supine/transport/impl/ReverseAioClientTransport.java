package vip.justlive.supine.transport.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.supine.codec.Serializer;

/**
 * 反向注册中心transport
 *
 * @author wubo
 */
public class ReverseAioClientTransport extends AioClientTransport {

  public ReverseAioClientTransport(Serializer serializer, ChannelContext channel) {
    super(null, serializer);
    this.channel = channel;
  }

  @Override
  public void connect(InetSocketAddress address) throws IOException {
    Transport transport = new Transport(channel);
    this.channel.addAttr(Transport.class.getName(), transport);
    try {
      transport.join(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e);
    } catch (ExecutionException | TimeoutException e) {
      throw new IOException(e);
    }
  }
}
