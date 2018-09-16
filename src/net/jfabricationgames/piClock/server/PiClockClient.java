package net.jfabricationgames.piClock.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.clock.Alarm;
import net.jfabricationgames.piClock.server.AlarmMessage.Type;

/**
 * The client (clock-side) that loads alarms from the server and sends locally created alarms to the server.
 */
public class PiClockClient {
	
	/**
	 * The time (in ms) to wait for a server acknowledge before resending the message
	 */
	public static final long WAIT_FOR_ACK = 5000;
	
	private final Logger LOGGER = LogManager.getLogger(PiClockClient.class);
	
	private Socket socket;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	public PiClockClient() {
		
	}
	
	public List<Alarm> loadAllAlarms() {
		try {
			establishConnection();
			if (connectionEstablished()) {
				AlarmMessage message = new PiClockAlarmMessage(AlarmMessage.Type.LOAD_ALL);
				AlarmMessage answer = requestAnswerMessage(message);
				if (answer.getType() != Type.LOAD_ALL) {
					LOGGER.warn("Received an answer message, but with the wrong Type (received: {}; expected: {})", answer.getType(),
							AlarmMessage.Type.LOAD_ALL);
				}
				return answer.getAlarmList();
			}
			return null;
		}
		finally {
			closeConnection();
		}
	}
	
	public void addAlarm(Alarm alarm) {
		try {
			establishConnection();
			if (connectionEstablished()) {
				List<Alarm> alarmList = new ArrayList<Alarm>(1);
				alarmList.add(alarm);
				AlarmMessage message = new PiClockAlarmMessage(AlarmMessage.Type.ADD, alarmList, true, alarm.getId());
				AlarmMessage answer = requestAnswerMessage(message);
				if (answer.getType() != AlarmMessage.Type.ACK || answer.getId() != alarm.getId()) {
					LOGGER.warn("Received answer but with unexpected Type or id (type: {}, id: {}; expected type: {}, expected id: {}",
							answer.getType(), answer.getId(), AlarmMessage.Type.ACK, alarm.getId());
				}
			}
		}
		finally {
			closeConnection();
		}
	}
	
	public void removeAlarm(Alarm alarm) {
		try {
			establishConnection();
			if (connectionEstablished()) {
				AlarmMessage message = new PiClockAlarmMessage(AlarmMessage.Type.REMOVE, null, true, alarm.getId());
				AlarmMessage answer = requestAnswerMessage(message);
				if (answer.getType() != AlarmMessage.Type.ACK || answer.getId() != alarm.getId()) {
					LOGGER.warn("Received answer but with unexpected Type or id (type: {}, id: {}; expected type: {}, expected id: {}",
							answer.getType(), answer.getId(), AlarmMessage.Type.ACK, alarm.getId());
				}
			}
		}
		finally {
			closeConnection();
		}
	}
	
	public void setAlarmActive(int id, boolean active) {
		try {
			establishConnection();
			if (connectionEstablished()) {
				AlarmMessage message = new PiClockAlarmMessage(AlarmMessage.Type.REMOVE, null, active, id);
				AlarmMessage answer = requestAnswerMessage(message);
				if (answer.getType() != AlarmMessage.Type.ACK || answer.getId() != id) {
					LOGGER.warn("Received answer but with unexpected Type or id (type: {}, id: {}; expected type: {}, expected id: {}",
							answer.getType(), answer.getId(), AlarmMessage.Type.ACK, id);
				}
			}
		}
		finally {
			closeConnection();
		}
	}
	
	private void sendMessage(AlarmMessage message) {
		if (socket != null) {
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
				LOGGER.error("An error occured while sending the message to the server", ioe);
				ioe.printStackTrace();
			}
		}
		else {
			LOGGER.warn("Could not send the message because the connection was not established (message: {})", message);
		}
	}
	
	private AlarmMessage requestAnswerMessage(AlarmMessage message) {
		sendMessage(message);
		Future<AlarmMessage> answerFuture = CompletableFuture.supplyAsync(() -> {
			try {
				return (AlarmMessage) inStream.readObject();
			}
			catch (IOException | ClassNotFoundException e) {
				LOGGER.error("An error occured while reading an object", e);
				e.printStackTrace();
				return null;
			}
		});
		try {
			AlarmMessage answer = answerFuture.get(WAIT_FOR_ACK, TimeUnit.MILLISECONDS);
			return answer;
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOGGER.error("An error occured while reading an object", e);
			e.printStackTrace();
			return null;
		}
	}
	
	private void establishConnection() {
		if (socket != null) {
			try {
				socket = new Socket(PiClockAlarmServer.HOST, PiClockAlarmServer.PORT);
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
			}
			catch (UnknownHostException uhe) {
				LOGGER.error("The host ({}) is unknown", PiClockAlarmServer.HOST, uhe);
				uhe.printStackTrace();
			}
			catch (IOException ioe) {
				LOGGER.error("Error while trying to establish the connection to the server", ioe);
				ioe.printStackTrace();
			}
		}
		else {
			LOGGER.debug("The establishConnection() method was called, but there is already an active connection");
		}
	}
	
	private boolean connectionEstablished() {
		return socket != null;
	}
	
	private void closeConnection() {
		if (connectionEstablished()) {
			try {
				//outStream.flush();
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
				finally {
					outStream = null;
					inStream = null;
					socket = null;
				}
			}
		}
	}
}