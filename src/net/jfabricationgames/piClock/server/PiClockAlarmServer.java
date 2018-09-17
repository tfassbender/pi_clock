package net.jfabricationgames.piClock.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.clock.Alarm;

/**
 * The alarm server that receives alarms, stores them in a database and sends them on to the local server (push messages).
 */
public class PiClockAlarmServer {
	
	public static final int PORT = 4711;
	public static final String HOST = "jfabricationgames.ddns.net";
	
	/**
	 * The host name of the PiClock (to send push messages)
	 */
	private String clockLocalServerHost;
	
	private final Logger LOGGER = LogManager.getLogger(PiClockAlarmServer.class);
	
	protected List<PiClockConnection> connections;
	
	protected ServerSocket serverSocket;
	protected Thread serverThread;
	
	protected PiClockAlarmServer(String clockLocalServerHost) {
		this.clockLocalServerHost = clockLocalServerHost;
		connections = new ArrayList<PiClockConnection>(5);
	}
	public PiClockAlarmServer() {
		this(null);
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
	
	protected PiClockServerInterpreter getInterpreterInstance() {
		return (message, connection) -> {
			switch (message.getType()) {
				case ADD:
					//TODO store the alarm on disk
					sendAck(message, connection);
					break;
				case LOAD_ALL:
					//TODO load all known alarms
					List<Alarm> alarms = new ArrayList<Alarm>(0);
					AlarmMessage answer = new PiClockAlarmMessage(AlarmMessage.Type.LOAD_ALL, alarms);
					try {
						connection.sendMessage(answer);
					}
					catch (IOException ioe) {
						LOGGER.error("An error occured while sending the alarm list back to the client", ioe);
						ioe.printStackTrace();
					}
					break;
				case REMOVE:
					//TODO remove the alarm from disk
					sendAck(message, connection);
					break;
				case SET_ACTIVE:
					//TODO set the active state of the alarm
					sendAck(message, connection);
					break;
				case ACK:
				default:
					LOGGER.warn("Interpreter received a message with an unknown or unexpected type: {}", message.getType());
					break;
			}
		};
	}
	
	private void sendAck(AlarmMessage message, PiClockConnection connection) {
		AlarmMessage ack = new PiClockAlarmMessage(AlarmMessage.Type.ACK, null, false, message.getId());
		try {
			connection.sendMessage(ack);
		}
		catch (IOException ioe) {
			LOGGER.error("An error occured while sending an ACK-message (ack to message: {})", message);
			ioe.printStackTrace();
		}
	}
}