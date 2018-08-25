package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.serial.PiClockSerialConnection;

/**
 * Manage the turning on and off of the display backlight to turn it of after some time of no actions. 
 */
public class DisplayBacklightManager implements TimeChangeListener {
	
	/**
	 * The time (in minutes) till the display is turned off when no actions are received.
	 * This time may vary up to 1 minute because updates are only done on whole minutes.
	 */
	public static final int DISPLAY_TURN_OFF_MINUTES = 3;
	
	private Logger LOGGER = LogManager.getLogger(DisplayBacklightManager.class);
	
	private PiClockSerialConnection serialConnection;
	private AlarmClockManager alarmManager;
	
	private LocalDateTime lastAction;
	
	public DisplayBacklightManager(PiClockSerialConnection serialConnection, AlarmClockManager alarmManager) {
		this.serialConnection = serialConnection;
		this.alarmManager = alarmManager;
		lastAction = LocalDateTime.now();
	}
	
	@Override
	public void timeChanged(LocalDateTime time) {
		LOGGER.debug("Received time changed event with parameter time: {}  \t(lastAction: {})", time, lastAction);
		if (alarmManager.isAnyAlarmPlaying()) {
			//don't turn the display off when an alarm is still playing
			LOGGER.info("Reseting display turn off because an alarm is still playing (current: {})", lastAction);
			reset();
		}
		if (lastAction.plusMinutes(DISPLAY_TURN_OFF_MINUTES).isBefore(time)) {
			LOGGER.info("Turning off backlight and reseting last action (current: {})", lastAction);
			turnOffDisplayBacklight();
			reset();//prevent to turn the display off every minute
		}
	}
	
	/**
	 * Reset the timer for the display to be turned off
	 */
	public void reset() {
		lastAction = LocalDateTime.now();
		LOGGER.debug("Reseted last action to: {}", lastAction);
	}
	
	private void turnOffDisplayBacklight() {
		serialConnection.setDisplayBacklightEnabled(false);
	}
}