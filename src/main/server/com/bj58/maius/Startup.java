package com.bj58.maius;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.bj58.maius.net.Acceptor;
import com.bj58.maius.net.Reactor;

public class Startup {

	private static final Logger log = Logger.getLogger(Startup.class);
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		log.info("===============================================");
		log.info("maius is ready to startup ...");
		// load config
		if (args.length <= 0) {
			log.error("Usage: java MultiPortEcho port [port port ...]");
			System.exit(1);
		}

		int ports[] = new int[args.length];

		for (int i = 0; i < args.length; ++i) {
			ports[i] = Integer.parseInt(args[i]);
		}
		
		log.info("Startup reactor ...");
		// startup reactor
		Reactor reactor = new Reactor();

		reactor.start();
		
		log.info("Startup acceptors ...");
		// startup server
		for (int i = 0; i < ports.length; ++i) {
			Acceptor acceptor = new Acceptor(ports[i]);

			acceptor.setReactor(reactor);

			acceptor.start();
		}
		log.info("===============================================");

	}

}
