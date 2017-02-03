package com.github.mdjc.chatserver.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.chatserver.domain.ClientHandler;
import com.github.mdjc.chatserver.domain.Server;
import com.github.mdjc.commons.Utils;

public class SocketServer extends Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);
	private static final int N_THREADS = Runtime.getRuntime().availableProcessors();

	private final int portNumber;
	private ThreadPoolExecutor executor;
	private ServerSocket serverSocket;

	public SocketServer(int portNumber) {
		this.portNumber = portNumber;
	}

	@SuppressWarnings("resource")
	@Override
	public void init() throws Exception {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(portNumber);
			LOGGER.info("Listening from {} in port {}", serverSocket.getInetAddress().getHostName(),
					serverSocket.getLocalPort());
			
			executor = new ThreadPoolExecutor(N_THREADS * 20, N_THREADS * 20, 0L, TimeUnit.MILLISECONDS,
					new SynchronousQueue<>());

			while (true) {
				Socket clientSocket = serverSocket.accept();
				ClientHandler handler = new SocketClientHandler(this, clientSocket);
				connect(handler);
				executor.submit(() -> handler.handle());
			}

		} finally {
			Utils.execIgnoreException(this::close);
		}
	}
	
	public void close() throws Exception {
		closeHandlers();
		executor.shutdown();
		executor.awaitTermination(4, TimeUnit.MINUTES);
		Utils.closeQuietly(serverSocket);
	}

	private void closeHandlers() {
		Iterator<ClientHandler> iterator = handlerIterator();
		while(iterator.hasNext()) {
			ClientHandler handler = iterator.next();
			handler.close();
		}
	}
}
