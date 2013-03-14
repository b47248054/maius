package com.bj58.maius.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * 响应read,write
 * 
 * @author liuzx
 * @version 2013-3-12 下午4:17:33
 */
public class Reactor extends Thread {

	private final Logger log = Logger.getLogger(Reactor.class);

	private final Selector selector;
	private BlockingQueue<SocketChannel> registerQueue;
	private ByteBuffer echoBuffer = ByteBuffer.allocate(1024);

	public Reactor() throws IOException {
		selector = Selector.open();
		registerQueue = new LinkedBlockingQueue<SocketChannel>();
	}

	public void register(SocketChannel channel) throws ClosedChannelException {
		registerQueue.add(channel);
		// channel.register(selector, SelectionKey.OP_READ);   注册新的channel到selector，务必重新唤醒阻塞的selector
		selector.wakeup();
	}

	@Override
	public void run() {
		while (true) {
			try {
				selector.select();
				SocketChannel channel = registerQueue.poll();
				if (null != channel) {
					channel.register(selector, SelectionKey.OP_READ);
				}
				Set<SelectionKey> keys = selector.selectedKeys();
				for (SelectionKey key : keys) {
					if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
						// Read the data
						SocketChannel sc = (SocketChannel) key.channel();

						// Echo data
						int bytesEchoed = 0;
						while (true) {
							echoBuffer.clear();

							int r = sc.read(echoBuffer);

							if (r <= 0) {
								break;
							}

							echoBuffer.flip();

							sc.write(echoBuffer);
							bytesEchoed += r;
						}

						log.info("Echoed " + bytesEchoed + " from " + sc);

					}
				}

			} catch (IOException e) {
				log.error(e);
			}

		}
	}
}
