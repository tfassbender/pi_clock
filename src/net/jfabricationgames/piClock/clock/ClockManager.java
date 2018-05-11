package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.piClock.serial.PiClockSerialConnection;

public class ClockManager implements Runnable {
	
	private int lastTimeMinutes;
	
	private Thread clockManager;
	
	private PiClockSerialConnection piClockConnection;
	
	private List<TimeChangeListener> timeChangeListeners;
	
	public ClockManager(PiClockSerialConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("The connection mussn't be null");
		}
		lastTimeMinutes = -1;
		piClockConnection = connection;
		timeChangeListeners = new ArrayList<TimeChangeListener>();
		clockManager = new Thread(this);
		clockManager.start();
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			//get the new time from the system clock
			LocalDateTime now = LocalDateTime.now();
			int timeMinutes = now.getMinute();
			if (timeMinutes != lastTimeMinutes) {
				updateClock(now);
				informListeners(now);
			}
		}
	}
	
	private void updateClock(LocalDateTime time) {
		piClockConnection.sendTime(time.getHour(), time.getMinute());
	}
	private void informListeners(LocalDateTime time) {
		for (TimeChangeListener listener : timeChangeListeners) {
			listener.timeChanged(time);
		}
	}
}