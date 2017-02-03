package com.github.mdjc.chatserver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.github.mdjc.chatserver.domain.ClientHandler;
import com.github.mdjc.chatserver.domain.Server;

public class ServerTest {
	private static final Random RANDOM = new Random();

	private Server server;

	@Before
	public void setUp() throws Exception {
		server = new InMemoryServer();
	}

	@Test
	public void testConnect() {
		List<ClientHandler> handlers = connectHandlers(5);
		testConnected(handlers);
	}

	@Test
	public void testDisconnect() {
		List<ClientHandler> handlers = connectHandlers(6);
		int randomIndex = RANDOM.nextInt(handlers.size());
		ClientHandler handler = handlers.get(randomIndex);
		handlers.remove(handler);

		server.disconnect(handler);
		assertNull(handler.getServer());
		testConnected(handlers);
	}

	@Test
	public void testBroadCast() {
		List<ClientHandler> handlers = connectHandlers(10);
		int randomIndex = RANDOM.nextInt(handlers.size());
		ClientHandler sender = handlers.get(randomIndex);
		String message = "Hello guys!";
		server.broadcast(message, sender);

		handlers.stream().filter(h -> h != sender)
				.forEach(h -> assertEquals(String.format("%s:%s", sender.getUsername(), message),
						((InMemoryClientHandler) h).getLastReceivedMessage()));
	}

	@Test
	public void testBroadcastUserLogsIn() {
		List<ClientHandler> handlers = connectHandlers(10);
		ClientHandler handler = handlers.get(handlers.size() - 1);
		server.broadcastUserLogsIn(handler);

		testLastReceivedMessage(handlers, handler, "_login_:%s");
	}

	@Test
	public void testBroadcastUserLogsOut() {
		List<ClientHandler> handlers = connectHandlers(10);
		ClientHandler handler = handlers.get(handlers.size() - 1);
		server.broadcastUserLogsOut(handler);

		testLastReceivedMessage(handlers, handler, "_logout_:%s");
	}

	private List<ClientHandler> connectHandlers(int n) {
		List<ClientHandler> handlers = new ArrayList<>(n);

		IntStream.range(0, n).forEach(i -> {
			ClientHandler handler = new InMemoryClientHandler(() -> "user" + i);
			handlers.add(handler);
			server.connect(handler);
			handler.handle();

			assertEquals(server, handler.getServer());
		});

		return handlers;
	}

	private void testConnected(List<ClientHandler> handlers) {
		Set<Integer> indexes = new HashSet<>();

		Iterator<ClientHandler> handlerIterator = server.handlerIterator();
		while (handlerIterator.hasNext()) {
			ClientHandler handler = handlerIterator.next();
			indexes.add(handlers.indexOf(handler));
		}

		assertEquals(handlers.size(), indexes.size());
	}

	private void testLastReceivedMessage(List<ClientHandler> handlers, ClientHandler handler, String format) {
		handlers.stream().filter(h -> h != handler)
				.forEach(h -> assertEquals(String.format(format, handler.getUsername()),
						((InMemoryClientHandler) h).getLastReceivedMessage()));
	}
}
