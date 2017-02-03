package com.github.mdjc.chatserver;

import java.util.function.Supplier;

import com.github.mdjc.chatserver.domain.ClientHandler;

public class InMemoryClientHandler extends ClientHandler {
	private final Supplier<String> usernameSupplier;
	private String lastReceivedMessage;

	public InMemoryClientHandler(Supplier<String> usernameSupplier) {
		this.usernameSupplier = usernameSupplier;
	}

	@Override
	public String awaitUsername() {
		return usernameSupplier.get();
	}

	@Override
	public void close() {
		
	}

	@Override
	public void receive(String message) {
		lastReceivedMessage = message;
	}

	public String getLastReceivedMessage() {
		return lastReceivedMessage;
	}
}
