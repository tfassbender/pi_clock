package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.serial.PiClockSerialConnection;

public class ClockManager implements Runnable {
	
	private Logger LOGGER = LogManager.getLogger(ClockManager.class);
	
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
		clockManager = new Thread(this, "ClockManagerThread");
		clockManager.start();
		LOGGER.info("Clock manager started");
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
				lastTimeMinutes = timeMinutes;
			}
			try {
				//wait for 50 milliseconds till the next time check
				Thread.sleep(50);
			}
			catch (InterruptedException ie) {
				//ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void stop() {
		LOGGER.info("Stopping clock manager");
		clockManager.interrupt();
	}
	
	private void updateClock(LocalDateTime time) {
		LOGGER.info("Sending clock update via serial connection");
		piClockConnection.sendTime(time.getHour(), time.getMinute());
	}
	private void informListeners(LocalDateTime time) {
		LOGGER.trace("Informing listeners about time change");
		for (TimeChangeListener listener : timeChangeListeners) {
			listener.timeChanged(time);
		}
	}
	
	public void addTimeChangeListener(TimeChangeListener listener) {
		timeChangeListeners.add(listener);
	}
	public boolean removeTimeChangeListener(TimeChangeListener listener) {
		return timeChangeListeners.remove(listener);
	}
}