package net.jfabricationgames.piClock.clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manage the loading, storing, ... of alarms using an {@code AlarmSupplier}.
 */
public class AlarmManager implements AlarmChangeListener {
	
	private final Logger LOGGER = LogManager.getLogger(AlarmManager.class);
	
	private AlarmSupplier alarmSupplier;
	
	private Map<Integer, Alarm> alarms;
	
	public AlarmManager(AlarmSupplier supplier) {
		this.alarmSupplier = supplier;
		this.alarms = new HashMap<Integer, Alarm>();
		supplier.addAlarmChangeListener(this);
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
		alarms.put(alarm.getId(), alarm);
		LOGGER.debug("Added alarm to the list; alarm: {}", alarm);
		alarmSupplier.addAlarm(alarm);
	}
	
	/**
	 * Removes an {@code Alarm} locally and informs the {@code AlarmSupplier}
	 */
	public void removeAlarm(Alarm alarm) {
		Alarm removed = alarms.remove(alarm.getId());
		if (removed != null) {
			LOGGER.debug("Removed alarm from list; alarm: {}", removed);
		}
		else {
			LOGGER.warn("The alarm that should be removed was not found (id: {} alarm: {})", alarm.getId(), alarm);
		}
		alarmSupplier.removeAlarm(alarm);
	}
	
	/**
	 * Load all {@code Alarm}s from the {@code AlarmSupplier}
	 * 
	 * The loading is implemented asynchronous to prevent blocking the main (graphics) thread
	 */
	public void loadAllAlarms() {
		Runnable loader = () -> {
			List<Alarm> allAlarms = alarmSupplier.loadAlarms();
			for (Alarm alarm : allAlarms) {
				alarms.put(alarm.getId(), alarm);
			}
		};
		Thread loaderThread = new Thread(loader, "AlarmLoadingThread");
		loaderThread.setDaemon(true);//don't block the shutting down of the program
		loaderThread.start();
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
		LOGGER.debug("Added alarm to the list (add command by remote); alarm: {}", alarm);
	}
	
	/**
	 * Informs the {@code AlarmChangeListener} that an {@code Alarm} was removed by a remote source
	 */
	@Override
	public void removeAlarmRemote(Alarm alarm) {
		Alarm removed = alarms.remove(alarm.getId());
		if (removed != null) {
			LOGGER.debug("Removed alarm from list (remove command by remote); alarm: {}", removed);
		}
		else {
			LOGGER.warn("The alarm that should be removed was not found (id: {} alarm: {})", alarm.getId(), alarm);
		}
	}
	
	/**
	 * Informs the {@code AlarmChangeListener} that an {@code Alarm}s active state was changed
	 */
	@Override
	public void setAlarmActiveRemote(int id, boolean active) {
		Alarm alarm = alarms.get(id);
		if (alarm != null) {
			LOGGER.debug("Setting active state of alarm: {} to {}", alarm, active);
			alarm.setActive(active);
		}
		else {
			LOGGER.warn("The alarm whichs active state should be changed is inknown (id: {})", id);
		}
	}
}