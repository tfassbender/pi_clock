package net.jfabricationgames.piClock.serial;

import java.util.Optional;

public class PiClockSerialConnection implements SerialMessageListener {
	
	private static final String COMMAND_SET_CLOCK = "C ";
	private static final String COMMAND_GET_TEMPERATURE = "C ";
	private static final String COMMAND_GET_HUMIDITY = "C ";
	private static final String COMMAND_END_SIGN = ";";
	
	public static final int DEFAULT_TIMEOUT = 50;
	
	private String lastReceivedMessage;
	
	private SerialConnection serialConnection;
	
	public PiClockSerialConnection() {
		serialConnection = new SerialConnection();
		if (!serialConnection.initialize()) {
			throw new IllegalStateException("Couldn't initialize the SerialConnection for unknown reasons.");
		}
	}
	
	@Override
	public void receiveSerialMessage(String message) {
		lastReceivedMessage = message;
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
	
	public Optional<Integer> getTemperature() {
		return getTemperature(DEFAULT_TIMEOUT);
	}
	public Optional<Integer> getTemperature(int timeout) {
		long startTime = System.currentTimeMillis();
		lastReceivedMessage = null;
		serialConnection.sendMessage(COMMAND_GET_TEMPERATURE + COMMAND_END_SIGN);
		try {
			while (lastReceivedMessage == null && (System.currentTimeMillis() - startTime) < timeout) {
				Thread.sleep(1);
			}
		}
		catch (InterruptedException ie) {
			//let the interrupted exception end the while loop
			ie.printStackTrace();
		}
		if (lastReceivedMessage != null) {
			try {
				int temperature = Integer.parseInt(lastReceivedMessage);
				return Optional.of(temperature);
			}
			catch (NumberFormatException nfe) {
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}
	
	public Optional<Integer> getHumidity() {
		return getHumidity(DEFAULT_TIMEOUT);
	}
	public Optional<Integer> getHumidity(int timeout) {
		long startTime = System.currentTimeMillis();
		lastReceivedMessage = null;
		serialConnection.sendMessage(COMMAND_GET_HUMIDITY + COMMAND_END_SIGN);
		try {
			while (lastReceivedMessage == null && (System.currentTimeMillis() - startTime) < timeout) {
				Thread.sleep(1);
			}
		}
		catch (InterruptedException ie) {
			//let the interrupted exception end the while loop
			ie.printStackTrace();
		}
		if (lastReceivedMessage != null) {
			try {
				int humidity = Integer.parseInt(lastReceivedMessage);
				return Optional.of(humidity);
			}
			catch (NumberFormatException nfe) {
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}
	
	public String getLastReceivedMessage() {
		return lastReceivedMessage;
	}
}