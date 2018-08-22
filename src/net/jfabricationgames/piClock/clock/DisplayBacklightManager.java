package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;

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
		if (alarmManager.isAnyAlarmPlaying()) {
			//don't turn the display off when an alarm is still playing
			reset();
		}
		if (lastAction.plusMinutes(DISPLAY_TURN_OFF_MINUTES).isAfter(time)) {
			turnOffDisplayBacklight();
			reset();//prevent to turn the display off every minute
		}
	}
	
	/**
	 * Reset the timer for the display to be turned off
	 */
	public void reset() {
		lastAction = LocalDateTime.now();
	}
	
	private void turnOffDisplayBacklight() {
		serialConnection.setDisplayBacklightEnabled(false);
	}
}