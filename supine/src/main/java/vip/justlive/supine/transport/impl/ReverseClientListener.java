package vip.justlive.supine.transport.impl;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.net.aio.AioListener;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.supine.codec.Serializer;

/**
 * 反向注册中心aio listener
 *
 * @author wubo
 */
@Slf4j
@RequiredArgsConstructor
public class ReverseClientListener implements AioListener {

  private final Serializer serializer;
  private final List<ReverseAioClientTransport> transports;

  @Override
  public void onConnected(ChannelContext channelContext) {
    log.info("server transport connected : {}, {}", channelContext, transports);
    if (transports != null) {
      ReverseAioClientTransport transport = new ReverseAioClientTransport(serializer,
          channelContext);
      channelContext.addAttr(ReverseAioClientTransport.class.getName(), transport);
      transports.add(transport);
      try {
        transport.connect(null);
      } catch (IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  @Override
  public void onClosed(ChannelContext channelContext) {
    if (transports != null) {
      ReverseAioClientTransport transport = (ReverseAioClientTransport) channelContext.getAttr(
          ReverseAioClientTransport.class.getName());
      if (transport != null) {
        transports.remove(transport);
      }
    }
  }
}
