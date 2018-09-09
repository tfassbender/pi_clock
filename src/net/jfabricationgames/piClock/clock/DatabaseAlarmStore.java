package net.jfabricationgames.piClock.clock;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.server.PiClockClient;

/**
 * Supply the alarms to the {@code AlarmManager} by loading them from the database (and using the {@code PiClockLocalServer} for push-messages).
 */
public class DatabaseAlarmStore implements AlarmSupplier {
	
	private final Logger LOGGER = LogManager.getLogger(DatabaseAlarmStore.class);
	
	private List<AlarmChangeListener> alarmChangeListeners = new ArrayList<AlarmChangeListener>();
	
	private PiClockClient client;
	
	public DatabaseAlarmStore() {
		client = new PiClockClient();
	}
	
	/**
	 * Add an {@code Alarm} that was created locally to the database server asynchronous
	 */
	@Override
	public void addAlarm(Alarm alarm) {
		Runnable alarmAdder = () -> client.addAlarm(alarm);
		Thread alarmAdderThread = new Thread(alarmAdder, "AlarmAdderThread");
		alarmAdderThread.setDaemon(true);
		alarmAdderThread.start();
	}
	
	/**
	 * Remove an {@code Alarm} that was created locally from the database server asynchronous
	 */
	@Override
	public void removeAlarm(Alarm alarm) {
		Runnable alarmRemover = () -> client.removeAlarm(alarm);
		Thread alarmRemoverThread = new Thread(alarmRemover, "AlarmRemoverThread");
		alarmRemoverThread.setDaemon(true);
		alarmRemoverThread.start();
	}
	
	/**
	 * Set the active state of an {@code Alarm} locally and update it in the database asynchronous
	 */
	@Override
	public void setAlarmActive(int id, boolean active) {
		Runnable alarmActiveStateChanger = () -> client.setAlarmActive(id, active);
		Thread alarmActiveStateChangerThread = new Thread(alarmActiveStateChanger, "AlarmActiveStateChangerThread");
		alarmActiveStateChangerThread.setDaemon(true);
		alarmActiveStateChangerThread.start();
	}
	
	/**
	 * Add an {@code Alarm} from a remote source (the database server) and inform the listeners
	 */
	@Override
	public void addAlarmRemote(Alarm alarm) {
		LOGGER.info("Received new alarm by remote source; informing all listeners (alarm: {} #listeners: {})", alarm, alarmChangeListeners.size());
		for (AlarmChangeListener alarmListener : alarmChangeListeners) {
			alarmListener.addAlarmRemote(alarm);
		}
	}
	
	/**
	 * Remove an {@code Alarm} from a remote source (the database server) and inform the listeners
	 */
	@Override
	public void removeAlarmRemote(Alarm alarm) {
		LOGGER.info("Received alarm delete command by remote source; informing all listeners (alarm: {} #listeners: {})", alarm,
				alarmChangeListeners.size());
		for (AlarmChangeListener alarmListener : alarmChangeListeners) {
			alarmListener.removeAlarmRemote(alarm);
		}
	}
	
	/**
	 * Set the active state of an {@code Alarm} from a remote source (the database server) and inform the listeners
	 */
	@Override
	public void setAlarmActiveRemote(int id, boolean active) {
		LOGGER.info("Received alarm active state change by remote source; informing all listeners (id: {} active: {} #listeners: {})", id, active,
				alarmChangeListeners.size());
		for (AlarmChangeListener alarmListener : alarmChangeListeners) {
			alarmListener.setAlarmActiveRemote(id, active);
		}
	}
	
	/**
	 * Load all {@code Alarm}s from the database server
	 * 
	 * This method should be called asynchronous because the loading from the database server could take some time
	 */
	@Override
	public List<Alarm> loadAlarms() {
		List<Alarm> alarms = client.loadAllAlarms();
		return alarms;
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