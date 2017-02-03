package com.github.mdjc.chatserver.domain;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Server {
	private final List<ClientHandler> handlers;

	public Server() {
		this.handlers = new CopyOnWriteArrayList<>();
	}

	public abstract void init() throws Exception;

	public void connect(ClientHandler handler) {
		handlers.add(handler);
		handler.setServer(this);
	}

	public void disconnect(ClientHandler handler) {
		handler.setServer(null);
		handlers.remove(handler);
	}

	public boolean loggedIn(String username) {
		return handlers.stream().filter(h -> h.getUsername() != null).filter(h -> username.equals(h.getUsername()))
				.findAny().isPresent();
	}

	public Iterator<ClientHandler> handlerIterator() {
		final List<ClientHandler> handlers = Collections.unmodifiableList(this.handlers);

		return new Iterator<ClientHandler>() {
			private int current = 0;

			@Override
			public boolean hasNext() {
				return current >= 0 && current < handlers.size();
			}

			@Override
			public ClientHandler next() {
				return handlers.get(current++);
			}

		};
	}

	public void broadcastUserLogsIn(ClientHandler handler) {
		send(defaultMessage("login", handler), handler);
	}

	public void broadcastUserLogsOut(ClientHandler handler) {
		send(defaultMessage("logout", handler), handler);
	}

	public void broadcast(String message, ClientHandler sender) {
		String senderMessage = String.format("%s:%s", sender.getUsername(), message);
		send(senderMessage, sender);
	}

	private void send(String message, ClientHandler sender) {
		handlers.stream().filter(h -> h != sender).forEach(h -> h.receive(message));
	}

	private String defaultMessage(String prefix, ClientHandler handler) {
		return String.format("_%s_:%s", prefix, handler.getUsername());
	}

	public abstract void close() throws Exception;
}
