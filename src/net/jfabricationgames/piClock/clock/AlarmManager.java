package net.jfabricationgames.piClock.clock;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage the loading, storing, ... of alarms using an {@code AlarmSupplier}.
 */
public class AlarmManager implements AlarmChangeListener {
	
	private AlarmSupplier alarmSupplier;
	
	private List<Alarm> alarms;
	
	public AlarmManager(AlarmSupplier supplier) {
		this.alarmSupplier = supplier;
		this.alarms = new ArrayList<Alarm>();
	}
	
	/**
	 * Returns a copy of the current alarm list (no deep copy)
	 */
	public List<Alarm> getAlarms() {
		return new ArrayList<Alarm>(alarms);
	}
	
	/**
	 * Adds an {@code Alarm} locally and informs the {@code AlarmSupplier}
	 */
	public void addAlarm(Alarm alarm) {
		alarmSupplier.addAlarm(alarm);
		alarms.add(alarm);
	}
	
	/**
	 * Removes an {@code Alarm} locally and informs the {@code AlarmSupplier}
	 */
	public void removeAlarm(Alarm alarm) {
		alarmSupplier.removeAlarm(alarm);
		alarms.remove(alarm);
	}
	
	/**
	 * Informs the supplier to store all alarms
	 */
	public void storeAlarms() {
		alarmSupplier.storeAlarms();
	}
	
	/**
	 * Informs the {@code AlarmChangeListener} that a new {@code Alarm} was added by a remote source
	 */
	@Override
	public void addAlarmRemote(Alarm alarm) {
		alarms.add(alarm);
	}
	
	/**
	 * Informs the {@code AlarmChangeListener} that an {@code Alarm} was removed by a remote source
	 */
	@Override
	public void removeAlarmRemote(Alarm alarm) {
		alarms.remove(alarm);
	}
}