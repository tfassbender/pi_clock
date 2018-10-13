package net.jfabricationgames.piClock.frame;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.audio.RPiAudioPlayer;
import net.jfabricationgames.piClock.clock.Alarm;
import net.jfabricationgames.piClock.clock.AlarmClockManager;
import net.jfabricationgames.piClock.clock.AlarmRepetition;
import net.jfabricationgames.piClock.clock.ClockManager;
import net.jfabricationgames.piClock.clock.DisplayBacklightManager;
import net.jfabricationgames.piClock.clock.PiClockAlarm;
import net.jfabricationgames.piClock.clock.TimeChangeListener;
import net.jfabricationgames.piClock.serial.PiClockSerialConnection;
import net.jfabricationgames.piClock.temperature.TemperatureChangeListener;
import net.jfabricationgames.piClock.temperature.TemperatureManager;

public class PiClockSwingController implements TimeChangeListener, TemperatureChangeListener{
	
	private Logger LOGGER = LogManager.getLogger(PiClockSwingController.class);
	
	private PiClockFrameSwing frame;
	
	private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
	
	private PiClockSerialConnection serialConnection;
	private ClockManager clockManager;
	private TemperatureManager temperatureManager;
	private RPiAudioPlayer audioPlayer;
	private AlarmClockManager alarmManager;
	private DisplayBacklightManager displayManager;
	
	public PiClockSwingController(PiClockFrameSwing frame) {
		this.frame = frame;
		
		LOGGER.info("Creating pi clock swing controller");
		LOGGER.info("Initializing serial connection, clock manager, tempeature manager, audio player and alarm manager");
		
		serialConnection = new PiClockSerialConnection();
		clockManager = new ClockManager(serialConnection);
		temperatureManager = new TemperatureManager(serialConnection);
		try {
			audioPlayer = new RPiAudioPlayer(serialConnection);
			alarmManager = new AlarmClockManager(clockManager, audioPlayer, this);
			displayManager = new DisplayBacklightManager(serialConnection, alarmManager);
			clockManager.addTimeChangeListener(alarmManager);
			clockManager.addTimeChangeListener(displayManager);
		}
		catch (IOException ioe) {
			LOGGER.error("Could not initialize audio player or alarm manager", ioe);
			ioe.printStackTrace();
		}
		clockManager.addTimeChangeListener(this);
		temperatureManager.addTemperatureChangeListener(this);
		
		frame.updateTrackList(audioPlayer.getTrackList());
	}
	
	@Override
	public void timeChanged(LocalDateTime time) {
		String timeText = time.format(timeFormat);
		if (timeText != null) {
			frame.setTime(timeText);			
		}
	}
	
	@Override
	public void temperatureChanged(double temperature) {
		String temperatureText = String.format("%.1f °C", temperature);
		frame.setTemperature(temperatureText);
	}
	
	@Override
	public void humidityChanged(double humidity) {
		String humidityText = String.format("%.1f", humidity) + " %";
		frame.setHumidity(humidityText);
	}
	
	public void addAlarm(int hour, int minute, AlarmRepetition repetition) {
		LOGGER.info("Creating new alarm for: {}:{} (repetition: {}", hour, minute, repetition);
		LocalDateTime alarmDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
		if (alarmDateTime.isBefore(LocalDateTime.now())) {
			alarmDateTime = alarmDateTime.plusDays(1);
		}
		Alarm alarm = new PiClockAlarm(alarmDateTime, alarmManager, true, repetition);
		alarmManager.addAlarm(alarm);
	}
	
	public void removeAlarm(Alarm alarm) {
		alarmManager.removeAlarm(alarm);
	}
	
	public void stopAlarm() {
		Alarm activeAlarm = alarmManager.getActiveAlarm();
		if (activeAlarm != null) {
			alarmManager.stopAlarm(activeAlarm);
		}
		else {
			alarmManager.stopAllAlarms();
		}
	}
	
	public void pauseAlarm() {
		Alarm activeAlarm = alarmManager.getActiveAlarm();
		if (activeAlarm != null) {
			int time = 60*5;//5 minutes
			alarmManager.pauseAlarm(activeAlarm, time);
		}
	}
	
	public List<Alarm> getAlarms() {
		return alarmManager.getAlarms();
	}
	
	public void setTimeTillNextAlarm(int hours, int minutes) {
		frame.setTimeTillNextAlarm(hours, minutes);
		updateAlarmList();
	}
	public void updateNextAlarmTime() {
		alarmManager.updateNextAlarm();
	}
	
	public void stopAll() {
		LOGGER.info("Stopping clock manager, temperature manager and closing serial connection");
		clockManager.stop();
		temperatureManager.stop();
		serialConnection.close();
	}
	
	public void storeAlarms() {
		alarmManager.storeAlarms();
	}
	
	public void updateAlarmList() {
		frame.updateAlarmList();
	}
	
	public void setPlayerVolume(int volume) {
		audioPlayer.setVolume(volume);
	}
	
	public AlarmClockManager getAlarmManager() {
		return alarmManager;
	}
	
	/**
	 * Enable or disable the microcontroller's alarm switch function.
	 */
	/*public void setAlarmSwitchEnabled(SerialMessageReceiver receiver, boolean enabled, int cause) {
		serialConnection.setAlarmSwitchEnabled(receiver, enabled, cause);
	}*/
	
	/**
	 * Enable or disable the power supply of the speaker amplifier using a relay 
	 */
	/*public void setSpeakerAmplifierEnabled(boolean enabled) {
		serialConnection.setSpeakerAmplifierEnabled(enabled);
	}*/
	
	public PiClockSerialConnection getPiClockSerialConnection() {
		return serialConnection;
	}
	
	public DisplayBacklightManager getDisplayManager() {
		return displayManager;
	}
	
	public RPiAudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
}