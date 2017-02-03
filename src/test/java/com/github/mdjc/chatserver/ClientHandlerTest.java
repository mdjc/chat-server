package com.github.mdjc.chatserver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.github.mdjc.chatserver.domain.AlreadyLoggedInException;
import com.github.mdjc.chatserver.domain.ClientHandler;
import com.github.mdjc.chatserver.domain.Server;

public class ClientHandlerTest {
	Server server;

	@Before
	public void setUp() throws Exception {
		server = new InMemoryServer();
	}

	@Test
	public void testHandleLogIn() {
		String username = "mirna";
		ClientHandler handler = new InMemoryClientHandler(() -> username);
		server.connect(handler);
		handler.handle();

		assertEquals(username, handler.getUsername());
	}

	@Test(expected = AlreadyLoggedInException.class)
	public void testHandleAlreadyLoggedIn() {
		String username = "mirna";
		ClientHandler handler = new InMemoryClientHandler(() -> username);
		server.connect(handler);
		ClientHandler handler2 = new InMemoryClientHandler(() -> username);
		server.connect(handler2);
		handler.handle();
		handler2.handle();
	}
}
