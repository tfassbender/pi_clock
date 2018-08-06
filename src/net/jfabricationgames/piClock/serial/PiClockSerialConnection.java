package net.jfabricationgames.piClock.serial;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PiClockSerialConnection implements SerialMessageListener {
	
	private Logger LOGGER = LogManager.getLogger(PiClockSerialConnection.class);
	
	private static final String COMMAND_SET_CLOCK = "C ";
	private static final String COMMAND_GET_TEMPERATURE = "T";
	private static final String COMMAND_GET_HUMIDITY = "H";
	private static final String COMMAND_SET_ALARM_SWITCH = "A ";
	private static final String COMMAND_SET_SPEAKER_AMPLIFIER = "S ";
	
	private static final String COMMAND_END_SIGN = ";";
	
	public static final int DEFAULT_TIMEOUT = 50;
	
	private String lastReceivedMessage;
	
	private SerialConnection serialConnection;
	
	private Queue<CallbackRequest> callbackRequests;
	private CallbackRequest alarmSwitchCallbackRequest;
	
	public PiClockSerialConnection() {
		serialConnection = new SerialConnection();
		if (!serialConnection.initialize()) {
			IllegalStateException ise = new IllegalStateException("Couldn't initialize the SerialConnection for unknown reasons.");
			LOGGER.error("Could not initialize the serial connection", ise);
			throw ise;
		}
		serialConnection.addSerialMessageListener(this);
		callbackRequests = new ArrayDeque<CallbackRequest>();
	}
	
	@Override
	public void receiveSerialMessage(String message) {
		LOGGER.trace("Received serial message: {}", message);
		lastReceivedMessage = message;
		CallbackRequest callback = callbackRequests.poll();
		if (callback != null) {
			callback.getCallback().receiveMessage(message, callback.getCause());
		}
		else if (alarmSwitchCallbackRequest != null) {
			alarmSwitchCallbackRequest.getCallback().receiveMessage(message, alarmSwitchCallbackRequest.getCause());
		}
		else {
			LOGGER.warn("Received a serial message but have no callback request in the queue");
			System.err.println("Received a serial message but have no callback requests in the queue...");
		}
	}
	
	public void close() {
		LOGGER.info("Closing serial connection");
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
		LOGGER.info("Sending time via serial connection (serial message: {})", clockText);
		serialConnection.sendMessage(clockText);
	}
	
	public void getTemperature(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		String message = COMMAND_GET_TEMPERATURE + COMMAND_END_SIGN;
		LOGGER.info("Sending temperatue request via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public void getHumidity(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		String message = COMMAND_GET_HUMIDITY + COMMAND_END_SIGN;
		LOGGER.info("Sending humidity request via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public void setAlarmSwitchEnabled(SerialMessageReceiver receiver, boolean enabled, int cause) {
		//no callback because the callback could come at any time and can't be queued
		String message = COMMAND_SET_ALARM_SWITCH;
		if (enabled) {
			//not added to the queue but always listening
			alarmSwitchCallbackRequest = new CallbackRequest(receiver, cause);
			message += '1';
		}
		else {
			alarmSwitchCallbackRequest = null;
			message += '0';
		}
		message += COMMAND_END_SIGN;
		LOGGER.info("Sending alarm switch state change via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public void setSpeakerAmplifierEnabled(boolean enabled) {
		String message = COMMAND_SET_SPEAKER_AMPLIFIER;
		if (enabled) {
			message += "1";
		}
		else {
			message += "0";
		}
		message += COMMAND_END_SIGN;
		LOGGER.info("Sending speaker amplifier state change via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public String getLastReceivedMessage() {
		return lastReceivedMessage;
	}
}