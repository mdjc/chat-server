package com.github.mdjc.chatserver;

import java.util.Properties;

import com.github.mdjc.chatserver.domain.Server;
import com.github.mdjc.chatserver.net.SocketServer;
import com.github.mdjc.commons.IOUtils;
import com.github.mdjc.commons.Utils;

public class App {
	public static void main(String[] args) throws Exception {
		Properties properties = IOUtils.loadConfig("app.properties");
		int portNumber = Integer.valueOf(properties.getProperty("port.number"));

		Server server = new SocketServer(portNumber);
		registerShutdownHook(server);
		server.init();
	}

	private static void registerShutdownHook(Server server) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Utils.execIgnoreException(() -> server.close());
			}
		});
	}

}
