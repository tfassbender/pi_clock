package net.jfabricationgames.piClock.clock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.jfabricationgames.piClock.audio.RPiAudioPlayer;

public class AlarmClockManager {
	
	private List<Alarm> alarms;
	private ClockManager clockManager;
	private RPiAudioPlayer player;
	private Alarm activeAlarm;
	
	public AlarmClockManager(ClockManager clockManager, RPiAudioPlayer player) {
		Objects.requireNonNull(clockManager, "The clock manager mussn't be null.");
		Objects.requireNonNull(player, "The audio player mussn't be null.");
		this.clockManager = clockManager;
		this.player = player;
		alarms = new ArrayList<Alarm>();
	}
	
	public void addAlarm(Alarm alarm) {
		alarms.add(alarm);
		clockManager.addTimeChangeListener(alarm);
	}
	public void removeAlarm(Alarm alarm) {
		alarms.remove(alarm);
		clockManager.removeTimeChangeListener(alarm);
	}
	
	public boolean isAlarmPlaying(Alarm alarm) {
		return activeAlarm != null && activeAlarm.equals(alarm);
	}
	
	public boolean stopAlarm(Alarm alarm) {
		if (activeAlarm != null && activeAlarm.equals(alarm)) {
			player.stop();
			activeAlarm = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	public void pauseAlarm(Alarm alarm, int seconds) {
		if (activeAlarm != null) {
			activeAlarm.pause(seconds);
		}
	}
	protected boolean pauseAlarm(Alarm alarm) {
		if (activeAlarm != null && activeAlarm.equals(alarm) && !activeAlarm.isPaused()) {
			player.pause();
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean playAlarm(Alarm alarm) {
		if (activeAlarm == null || activeAlarm.equals(alarm)) {
			try {
				player.play();
				activeAlarm = alarm;
				return true;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public Alarm getActiveAlarm() {
		return activeAlarm;
	}
	
	public List<Alarm> getAlarms() {
		return alarms;
	}
}