package net.jfabricationgames.piClock.clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage the loading, storing, ... of alarms using an {@code AlarmSupplier}.
 */
public class AlarmManager implements AlarmChangeListener {
	
	private AlarmSupplier alarmSupplier;
	
	private Map<Integer, Alarm> alarms;
	
	public AlarmManager(AlarmSupplier supplier) {
		this.alarmSupplier = supplier;
		this.alarms = new HashMap<Integer, Alarm>();
	}
	
	/**
	 * Returns a copy of the current alarm list (no deep copy)
	 */
	public List<Alarm> getAlarms() {
		return new ArrayList<Alarm>(alarms.values());
	}
	
	/**
	 * Adds an {@code Alarm} locally and informs the {@code AlarmSupplier}
	 */
	public void addAlarm(Alarm alarm) {
		alarmSupplier.addAlarm(alarm);
		alarms.put(alarm.getId(), alarm);
	}
	
	/**
	 * Removes an {@code Alarm} locally and informs the {@code AlarmSupplier}
	 */
	public void removeAlarm(Alarm alarm) {
		alarmSupplier.removeAlarm(alarm);
		alarms.remove(alarm.getId());
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
		alarms.put(alarm.getId(), alarm);
	}
	
	/**
	 * Informs the {@code AlarmChangeListener} that an {@code Alarm} was removed by a remote source
	 */
	@Override
	public void removeAlarmRemote(Alarm alarm) {
		alarms.remove(alarm.getId());
	}
}