package net.jfabricationgames.piClock.clock;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.jfabricationgames.piClock.audio.RPiAudioPlayer;
import net.jfabricationgames.piClock.frame.PiClockSwingController;

public class AlarmClockManager implements TimeChangeListener {
	
	private static final String PROPERTIES_DIR = ".pi_clock_properties";
	private static final String ALARM_FILE = "alarms";
	
	private List<Alarm> alarms;
	private ClockManager clockManager;
	private RPiAudioPlayer player;
	private Alarm activeAlarm;
	private PiClockSwingController controller;
	
	public AlarmClockManager(ClockManager clockManager, RPiAudioPlayer player, PiClockSwingController controller) {
		Objects.requireNonNull(clockManager, "The clock manager mussn't be null.");
		Objects.requireNonNull(player, "The audio player mussn't be null.");
		Objects.requireNonNull(controller, "The frame mussn't be null.");
		this.clockManager = clockManager;
		this.player = player;
		this.controller = controller;
		alarms = new ArrayList<Alarm>();
		loadAlarms();
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
				activateScreen();
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
	
	public void updateNextAlarm() {
		Collections.sort(alarms);
		if (!alarms.isEmpty() && alarms.get(0).isActive()) {
			LocalDateTime nextAlarmTime = alarms.get(0).getDateTime();
			LocalDateTime now = LocalDateTime.now();
			long hours = now.until(nextAlarmTime, ChronoUnit.HOURS);
			long minutes = now.until(nextAlarmTime, ChronoUnit.MINUTES) % 60;
			if (hours < 24) {
				controller.setTimeTillNextAlarm((int) hours, (int) minutes);
			}
			else {
				//no active alarm in the next 24 hours
				controller.setTimeTillNextAlarm(-1, -1);
			}
		}
		else {
			//no alarm active
			controller.setTimeTillNextAlarm(-1, -1);
		}
	}
	
	public void storeAlarms() {
		try (ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(new File(PROPERTIES_DIR + "/" + ALARM_FILE)))) {
			//write the number of alarms and the alarm objects to the file
			writer.writeInt(alarms.size());
			for (Alarm alarm : alarms) {
				writer.writeObject(alarm);
			}
		}
		catch (IOException ioe) {
			System.err.println("Couldn't store the alarms.");
			ioe.printStackTrace();
		}
	}
	public void loadAlarms() {
		try (ObjectInputStream reader = new ObjectInputStream(new FileInputStream(new File(PROPERTIES_DIR + "/" + ALARM_FILE)))) {
			//read the number of alarm objects
			int alarmCount = reader.readInt();
			List<Alarm> loadedAlarms = new ArrayList<Alarm>();
			//load all alarm objects from the file
			for (int i = 0; i < alarmCount; i++) {
				Alarm loaded = (Alarm) reader.readObject();
				loadedAlarms.add(loaded);
			}
			//check the active state of all alarms and add them to the alarm list
			for (Alarm alarm : loadedAlarms) {
				if (alarm.isActive() && alarm.getDateTime().isBefore(LocalDateTime.now())) {
					alarm.setActive(false);
				}
				PiClockAlarm piClockAlarm = new PiClockAlarm(alarm.getDateTime(), this, alarm.isActive(), alarm.getRepetition()); 
				alarms.add(piClockAlarm);
				clockManager.addTimeChangeListener(piClockAlarm);
			}
		}
		catch (IOException | ClassNotFoundException e) {
			System.err.println("Couldn't load the alarms from file");
			e.printStackTrace();
		}
	}
	
	private void activateScreen() {
		//wake up the screen by moving the cursor
		try {
			Robot robot = new Robot();
			robot.mouseMove(100, 100);
		}
		catch (AWTException e) {
			//e.printStackTrace();
		}
	}
	
	public Alarm getActiveAlarm() {
		return activeAlarm;
	}
	
	public List<Alarm> getAlarms() {
		return alarms;
	}

	@Override
	public void timeChanged(LocalDateTime time) {
		updateNextAlarm();
	}
}