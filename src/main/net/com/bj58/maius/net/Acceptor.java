package com.bj58.maius.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 监听端口
 * @author liuzx
 * @version  2013-3-12 下午3:13:45
 */
public class Acceptor extends Thread{

	private static final Logger log = Logger.getLogger(Acceptor.class);
	private final int port;
	private final Selector selector;
	private Reactor reactor;
	
	public Acceptor(int port) throws IOException {
		this.port = port;
		selector = Selector.open();
		
		InetSocketAddress addr = new InetSocketAddress(this.port);
		
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		
		ServerSocket socket = channel.socket();
		
		socket.bind(addr);
		
		channel.register(selector, SelectionKey.OP_ACCEPT);
		
		log.info("Going to listen on " + this.port);
	}
	
	public void setReactor(Reactor reactor) {
		this.reactor = reactor;
	}
	
	@Override
	public void run() {
		
		while(true){
			try {
				this.selector.select();
				
				Set<SelectionKey> keys = selector.selectedKeys();
				
				try {
					for(SelectionKey key : keys){
						if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
							// Accept the new connection
							ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
							SocketChannel channel = serverChannel.accept();
							channel.configureBlocking(false);
							
							log.info(Thread.currentThread().getName() + " Got connection from " + channel);
							
							// Add the new connection to the selector
//							channel.register(selector, SelectionKey.OP_READ);
							reactor.register(channel);
							
							
						} else {
							key.cancel();
						}
					}
				} finally {
					keys.clear();
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
}
