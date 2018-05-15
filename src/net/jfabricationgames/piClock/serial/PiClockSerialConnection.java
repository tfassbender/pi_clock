package net.jfabricationgames.piClock.serial;

import java.util.ArrayDeque;
import java.util.Queue;

public class PiClockSerialConnection implements SerialMessageListener {
	
	private static final String COMMAND_SET_CLOCK = "C ";
	private static final String COMMAND_GET_TEMPERATURE = "T";
	private static final String COMMAND_GET_HUMIDITY = "H";
	private static final String COMMAND_END_SIGN = ";";
	
	public static final int DEFAULT_TIMEOUT = 50;
	
	private String lastReceivedMessage;
	
	private SerialConnection serialConnection;
	
	private Queue<CallbackRequest> callbackRequests;
	
	public PiClockSerialConnection() {
		serialConnection = new SerialConnection();
		if (!serialConnection.initialize()) {
			throw new IllegalStateException("Couldn't initialize the SerialConnection for unknown reasons.");
		}
		serialConnection.addSerialMessageListener(this);
		callbackRequests = new ArrayDeque<CallbackRequest>();
	}
	
	@Override
	public void receiveSerialMessage(String message) {
		lastReceivedMessage = message;
		CallbackRequest callback = callbackRequests.poll();
		if (callback != null) {
			callback.getCallback().receiveMessage(message, callback.getCause());
		}
		else {
			System.err.println("Received a serial message but have no callback requests in the queue...");
		}
	}
	
	public void close() {
		serialConnection.close();
	}
	
	public void sendTime(int hour, int minute) throws IllegalArgumentException {
		if (hour < 0 || hour > 23) {
			throw new IllegalArgumentException("An hour of the day should be something between 0 and 23 (not " + hour + ")");
		}
		if (minute < 0 || minute > 59) {
			throw new IllegalArgumentException("A minute of an hour should be something between 0 and 59 (not " + hour + ")");
		}
		String clockText = COMMAND_SET_CLOCK;
		//add leading zeros to the clock text
		if (hour < 10) {
			clockText += "0";
		}
		clockText += hour;
		if (minute < 10) {
			clockText += "0";
		}
		clockText += minute;
		clockText += COMMAND_END_SIGN;
		//send the new time to via the serial port
		serialConnection.sendMessage(clockText);
	}
	
	public void getTemperature(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		serialConnection.sendMessage(COMMAND_GET_TEMPERATURE + COMMAND_END_SIGN);
	}
	
	public void getHumidity(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		serialConnection.sendMessage(COMMAND_GET_HUMIDITY + COMMAND_END_SIGN);
	}
	
	public String getLastReceivedMessage() {
		return lastReceivedMessage;
	}
}