package net.jfabricationgames.piClock.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A (server-side) connection to the client, holding the streams.
 */
public class PiClockConnection {
	
	private PiClockServerInterpreter interpreter;
	
	private Socket socket;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	private Thread connection;
	
	private final Logger LOGGER = LogManager.getLogger(PiClockConnection.class);
	
	public PiClockConnection(Socket socket, PiClockServerInterpreter interpreter) throws IOException {
		this.socket = socket;
		outStream = new ObjectOutputStream(socket.getOutputStream());
		inStream = new ObjectInputStream(socket.getInputStream());
		startServerThread();
	}
	
	private void startServerThread() throws IllegalStateException {
		if (connection != null) {
			throw new IllegalStateException("Can't initialize the connection when a connection is already running");
		}
		connection = new Thread(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					Object clientRequest = inStream.readObject();
					if (clientRequest instanceof AlarmMessage) {
						//pass on all messages to the interpreter
						interpreter.interpreteMessage((AlarmMessage) clientRequest, this);
					}
					else {
						LOGGER.warn("Received a message that was no AlarmMessage; Message couln't be handled");
					}
				}
			}
			catch (IOException | ClassNotFoundException e) {
				LOGGER.error("An error occured while reading a client message (or waiting for a message)", e);
			}
		});
		connection.start();
	}
	
	public void sendMessage(AlarmMessage message) throws IOException {
		//reset the stream to avoid false-repeated sending
		try {
			outStream.reset();
		}
		catch (IOException ioe) {
			LOGGER.warn("An error occured while reseting the output stream", ioe);
			ioe.printStackTrace();
		}
		//send the message
		try {
			outStream.writeObject(message);
			outStream.flush();
		}
		catch (IOException ioe) {
			LOGGER.error("An error occured while sending the message to the client", ioe);
			throw ioe;
		}
	}
	
	public void closeConnection() {
		try {
			outStream.flush();
			outStream.close();
			inStream.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			LOGGER.error("An error occured while closing the streams", ioe);
		}
		finally {
			try {
				socket.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				LOGGER.error("An error occured while closing the socket", ioe);
			}
		}
	}
}