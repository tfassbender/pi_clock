package net.jfabricationgames.piClock.clock;

import java.util.ArrayList;
import java.util.List;

/**
 * Supply the alarms to the {@code AlarmManager} by loading them from the database (and using the {@code PiClockLocalServer} for push-messages).
 */
public class DatabaseAlarmStore implements AlarmSupplier {
	
	private List<AlarmChangeListener> alarmChangeListeners = new ArrayList<AlarmChangeListener>();
	
	/**
	 * Add an {@code Alarm} that was created locally to the database server asynchronous
	 */
	@Override
	public void addAlarm(Alarm alarm) {
		//TODO add the alarm to the database asynchronous
	}
	
	/**
	 * Remove an {@code Alarm} that was created locally from the database server asynchronous
	 */
	@Override
	public void removeAlarm(Alarm alarm) {
		//TODO remove the alarm from the database asynchronous
	}
	
	/**
	 * Add an {@code Alarm} from a remote source (the database server) and inform the listeners
	 */
	@Override
	public void addAlarmRemote(Alarm alarm) {
		for (AlarmChangeListener alarmListener : alarmChangeListeners) {
			alarmListener.addAlarmRemote(alarm);
		}
	}
	
	/**
	 * Remove an {@code Alarm} from a remote source (the database server) and inform the listeners
	 */
	@Override
	public void removeAlarmRemote(Alarm alarm) {
		for (AlarmChangeListener alarmListener : alarmChangeListeners) {
			alarmListener.removeAlarmRemote(alarm);
		}
	}
	
	/**
	 * Load all {@code Alarm}s from the database server
	 * 
	 * This method should be called asynchronous because the loading from the database server could take some time
	 */
	@Override
	public List<Alarm> loadAlarms() {
		//TODO load the alarms from the database
		return null;
	}
	
	/**
	 * Does nothing in this implementation because the alarms are automatically added to the database server when they are received
	 */
	@Override
	public void storeAlarms() {
		//do nothing here because all alarms are automatically passed on to the database server
	}
	
	@Override
	public void addAlarmChangeListener(AlarmChangeListener listener) {
		alarmChangeListeners.add(listener);
	}
	
	@Override
	public void removeAlarmChangeListener(AlarmChangeListener listener) {
		alarmChangeListeners.remove(listener);
	}
}