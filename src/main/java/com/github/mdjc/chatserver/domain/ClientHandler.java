package com.github.mdjc.chatserver.domain;

public abstract class ClientHandler {
	private String username;
	private Server server;

	public String getUsername() {
		return username;
	}

	public Server getServer() {
		return server;
	}

	protected void setServer(Server server) {
		this.server = server;
	}

	public void handle() {
		logIn();
	}

	protected void logIn() {
		String username = awaitUsername();

		if (server.loggedIn(username)) {
			throw new AlreadyLoggedInException(username + " already Logged In");
		}

		this.username = username;
	}

	protected abstract String awaitUsername();

	public abstract void receive(String message);

	public abstract void close();
}
