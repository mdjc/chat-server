package com.github.mdjc.chatserver.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mdjc.chatserver.domain.AlreadyLoggedInException;
import com.github.mdjc.chatserver.domain.ClientHandler;
import com.github.mdjc.chatserver.domain.Server;
import com.github.mdjc.commons.IOUtils;
import com.github.mdjc.commons.Utils;

public class SocketClientHandler extends ClientHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

	private final Server server;
	private final Socket clientSocket;
	private final BufferedWriter writer;
	private final BufferedReader reader;

	public SocketClientHandler(Server server, Socket clientSocket) throws IOException {
		this.server = server;
		this.clientSocket = clientSocket;
		writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
		reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	@Override
	public void handle() {
		try {
			super.handle();
			IOUtils.writeAndFlush(writer, "OK");
			server.broadcastUserLogsIn(this);
			startAcceptingMessages();
		} catch (AlreadyLoggedInException e) {
			Utils.execIgnoreException(() -> IOUtils.writeAndFlush(writer, ("AlreadyLoggedIn")));
		} catch (IOException e) {
			LOGGER.error("Exception user:{}, exceptionMessage:{} ", getUsername(), e.getMessage());
		} finally {
			close();
		}
	}

	@Override
	public void receive(String message) {
		try {
			IOUtils.writeAndFlush(writer, message);
		} catch (IOException e) {
			LOGGER.error("Exception sending message {}, Exception:{} ", message, e);
		}
	}

	@Override
	public void close() {
		Utils.closeQuietly(writer, reader, clientSocket);
		getServer().disconnect(this);
	}

	@Override
	protected String awaitUsername() {
		String username = null;

		try {
			String line = reader.readLine();

			if (line == null) {
				IOUtils.writeAndFlush(writer, "Login Required");
			}

			if (!line.substring(0, 6).equalsIgnoreCase("login:")) {
				IOUtils.writeAndFlush(writer, "Invalid Login");
			}

			username = line.substring(6);
		} catch (Exception e) {
			LOGGER.error("Exception for user:{} exception: {}", username, e);
		}

		return username;
	}

	private void startAcceptingMessages() throws IOException {
		String message = "";
		while ((message = reader.readLine()) != null) {
			if (message.equalsIgnoreCase("_logout:" + getUsername() + "_")) {
				LOGGER.debug("user: {} requested logout", getUsername());
				server.broadcastUserLogsOut(this);
				return;
			}

			LOGGER.debug("User:{} sends: {}", getUsername(), message);
			server.broadcast(message, this);
		}
	}
}
