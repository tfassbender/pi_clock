package net.jfabricationgames.piClock.frame;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import net.jfabricationgames.piClock.audio.RPiAudioPlayer;
import net.jfabricationgames.piClock.clock.Alarm;
import net.jfabricationgames.piClock.clock.AlarmClockManager;
import net.jfabricationgames.piClock.clock.AlarmRepetition;
import net.jfabricationgames.piClock.clock.ClockManager;
import net.jfabricationgames.piClock.clock.PiClockAlarm;
import net.jfabricationgames.piClock.clock.TimeChangeListener;
import net.jfabricationgames.piClock.serial.PiClockSerialConnection;
import net.jfabricationgames.piClock.temperature.TemperatureChangeListener;
import net.jfabricationgames.piClock.temperature.TemperatureManager;

public class PiClockSwingController implements TimeChangeListener, TemperatureChangeListener{
	
	private PiClockFrameSwing frame;
	
	private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
	
	private PiClockSerialConnection serialConnection;
	private ClockManager clockManager;
	private TemperatureManager temperatureManager;
	private RPiAudioPlayer audioPlayer;
	private AlarmClockManager alarmManager;
	
	public PiClockSwingController(PiClockFrameSwing frame) {
		this.frame = frame;
		
		serialConnection = new PiClockSerialConnection();
		clockManager = new ClockManager(serialConnection);
		temperatureManager = new TemperatureManager(serialConnection);
		try {
			audioPlayer = new RPiAudioPlayer();
			alarmManager = new AlarmClockManager(clockManager, audioPlayer, this);
			clockManager.addTimeChangeListener(alarmManager);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		clockManager.addTimeChangeListener(this);
		temperatureManager.addTimeChangeListener(this);
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
	
	public AlarmClockManager getAlarmManager() {
		return alarmManager;
	}
}