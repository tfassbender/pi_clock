package net.jfabricationgames.piClock.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The alarm server that receives alarms, stores them in a database and sends them on to the local server (push messages).
 */
public class PiClockAlarmServer {
	
	public static final int PORT = 4711;
	public static final String HOST = "jfabricationgames.ddns.net";
	
	private final Logger LOGGER = LogManager.getLogger(PiClockAlarmServer.class);
	
	private List<PiClockConnection> connections;
	
	private ServerSocket serverSocket;
	private Thread serverThread;
	
	public PiClockAlarmServer() {
		connections = new ArrayList<PiClockConnection>(5);
	}
	
	public void startServer() throws IOException {
		serverSocket = new ServerSocket(PORT);
		serverThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket connectionSocket = serverSocket.accept();
					PiClockConnection connection = new PiClockConnection(connectionSocket, getInterpreterInstance());
					connections.add(connection);
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
					LOGGER.warn("An exception occured while accepting a new connection (could only be a closed socket...)", ioe);
				}
			}
		});
		serverThread.start();
	}
	
	/**
	 * Stop the execution of the server.
	 */
	public void stopServer() throws IOException {
		serverThread.interrupt();
		serverSocket.close();
	}
	
	private PiClockServerInterpreter getInterpreterInstance() {
		return (message, connection) -> {
			//TODO
		};
	}
}