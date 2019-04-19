package net.jfabricationgames.piClock.serial;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.frame.PiClockSwingController;

public class PiClockSerialConnection implements SerialMessageListener {
	
	private Logger LOGGER = LogManager.getLogger(PiClockSerialConnection.class);
	
	private static final String COMMAND_SET_CLOCK = "C ";
	private static final String COMMAND_GET_TEMPERATURE = "T";
	private static final String COMMAND_GET_HUMIDITY = "H";
	private static final String COMMAND_SET_ALARM_SWITCH = "A ";
	private static final String COMMAND_SET_SPEAKER_AMPLIFIER = "S ";
	private static final String COMMAND_SET_DISPLAY_BACKLIGHT = "B ";
	private static final String COMMAND_SHOW_TIME_5_SECONDS = "D ";
	
	private static final String SHOW_NONE_TEXT = "NONE";
	
	private static final String COMMAND_END_SIGN = ";";
	
	public static final int DEFAULT_TIMEOUT = 50;
	
	private String lastReceivedMessage;
	
	private SerialConnection serialConnection;
	
	private Queue<CallbackRequest> callbackRequests;
	private CallbackRequest alarmSwitchCallbackRequest;
	
	private PiClockSwingController swingController;
	
	public PiClockSerialConnection(PiClockSwingController swingController) {
		this.swingController = swingController;
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
		//first check whether the message is from a remote alarm controller because these don't use callbacks
		if (swingController != null && swingController.isRemoteAlarmMessage(message)) {
			swingController.processRemoteAlarmMessage(message);
		}
		else {
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
		LOGGER.trace("Sending time via serial connection (serial message: {})", clockText);
		serialConnection.sendMessage(clockText);
	}
	
	/**
	 * Shows a given time on the clock display for 5 seconds. Used for the remote alarm controller to display successful submission.
	 * 
	 * @param hour
	 *        The hour that is displayed (from 0 to 23). If the parameter is set to -1 the text "NONE" will be displayed. (all other invalid inputs
	 *        cause exceptions)
	 * 
	 * @param minute
	 *        The minute that is displayed (from 0 to 59). if the parameter is set to -1 the text "NONE" will be displayed. (all other invalid inputs
	 *        cause exceptions)
	 */
	public void showTimeForFiveSeconds(int hour, int minute) {
		LOGGER.debug("Showing time for 5 seconds: hour: " + hour + " minute: " + minute);
		String sendText = COMMAND_SHOW_TIME_5_SECONDS;
		if (hour == -1 || minute == -1) {
			sendText += SHOW_NONE_TEXT;
		}
		else if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
			LOGGER.error("Invalid time format found to be displayed: hour: " + hour + " minute: " + minute);
			throw new IllegalArgumentException("The time format " + hour + ":" + minute + " is not valid");
		}
		else {
			if (hour < 10) {
				sendText += "0" + hour;
			}
			else {
				sendText += hour;				
			}
			
			if (minute < 10) {
				sendText += "0" + minute;
			}
			else {
				sendText += minute;				
			}
		}
		sendText += COMMAND_END_SIGN;
		
		LOGGER.trace("Sending text via serial connection (serial message: {})", sendText);
		serialConnection.sendMessage(sendText);
	}
	
	public void getTemperature(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		String message = COMMAND_GET_TEMPERATURE + COMMAND_END_SIGN;
		LOGGER.trace("Sending temperatue request via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public void getHumidity(SerialMessageReceiver callback, int cause) {
		lastReceivedMessage = null;
		callbackRequests.offer(new CallbackRequest(callback, cause));
		String message = COMMAND_GET_HUMIDITY + COMMAND_END_SIGN;
		LOGGER.trace("Sending humidity request via serial connection (serial message: {})", message);
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
	
	public void setDisplayBacklightEnabled(boolean backlightOn) {
		String message = COMMAND_SET_DISPLAY_BACKLIGHT;
		if (backlightOn) {
			message += "1";
		}
		else {
			message += "0";
		}
		message += COMMAND_END_SIGN;
		LOGGER.info("Sending display backlight state change via serial connection (serial message: {})", message);
		serialConnection.sendMessage(message);
	}
	
	public String getLastReceivedMessage() {
		return lastReceivedMessage;
	}
}