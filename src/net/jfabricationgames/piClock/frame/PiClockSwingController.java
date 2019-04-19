package net.jfabricationgames.piClock.frame;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

public class PiClockSwingController implements TimeChangeListener, TemperatureChangeListener {
	
	private Logger LOGGER = LogManager.getLogger(PiClockSwingController.class);
	
	//the message codes to send to the alarm clock for remote alarms (need pattern recognition because no callback can be used)
	private static final String REMOTE_ALARM_CODE_SET_ALARM = "REMOTE_ALARM_SET ";//+ alarm time as string
	private static final String REMOTE_ALARM_CODE_SHOW_NEXT_ALARM = "REMOTE_ALARM_SHOW";
	private static final String REMOTE_ALARM_CODE_DELETE_ALL = "REMOTE_ALARM_DELETE_ALL";
	
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
		
		serialConnection = new PiClockSerialConnection(this);
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
		LOGGER.info("Received order to create new alarm for: {}:{} (repetition: {})", hour, minute, repetition);
		
		LocalDateTime alarmDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute));
		if (alarmDateTime.isBefore(LocalDateTime.now())) {
			alarmDateTime = alarmDateTime.plusDays(1);
		}
		Alarm alarm = new PiClockAlarm(alarmDateTime, alarmManager, true, repetition);
		
		//check whether there is already an alarm at this time
		Optional<Alarm> equalAlarm = alarmManager.getEqualAlarm(alarm);
		
		if (equalAlarm.isPresent()) {
			LOGGER.info(
					"Found alarm equal to the new alarm. Activating the equal alarm instead of adding a new one (Alarm activated: {}:{} (repetition: {}))",
					hour, minute, repetition);
			//activate the equal alarm instead of adding a new one
			equalAlarm.get().setActive(true);
		}
		else {
			LOGGER.info("Creating new alarm for: {}:{} (repetition: {})", hour, minute, repetition);
			//add a new alarm
			alarmManager.addAlarm(alarm);
		}
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
			int time = 60 * 5;//5 minutes
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
	
	public boolean isRemoteAlarmMessage(String message) {
		return message.startsWith(REMOTE_ALARM_CODE_SET_ALARM) || message.startsWith(REMOTE_ALARM_CODE_SHOW_NEXT_ALARM)
				|| message.startsWith(REMOTE_ALARM_CODE_DELETE_ALL);
	}
	
	public void processRemoteAlarmMessage(String message) {
		if (message.startsWith(REMOTE_ALARM_CODE_SET_ALARM)) {
			try {
				String timeCode = message.substring(message.length() - 4);
				String hourCode = timeCode.substring(0, 2);
				String minuteCode = timeCode.substring(2);
				
				LOGGER.debug("Received remote alarm update: timeCode: " + timeCode + " hourCode: " + hourCode + " minuteCode: " + minuteCode);
				
				//hour codes like 0700 are only shown as 700
				if (hourCode.charAt(0) == ' ') {
					hourCode = "0" + hourCode.substring(1);
				}
				
				int alarmHour = Integer.parseInt(hourCode);
				int alarmMinute = Integer.parseInt(minuteCode);
				
				LOGGER.info("Adding new alarm (from remote): " + alarmHour + ":" + alarmMinute);
				addAlarm(alarmHour, alarmMinute, AlarmRepetition.NONE);
				//show the next alarm time on the clock display
				showNextAlarmTime();
			}
			catch (NumberFormatException | IndexOutOfBoundsException e) {
				LOGGER.error("Error while decoding the new alarm", e);
			}
		}
		else if (message.startsWith(REMOTE_ALARM_CODE_SHOW_NEXT_ALARM)) {
			LOGGER.info("Received remote show alarm message");
			showNextAlarmTime();
		}
		else if (message.startsWith(REMOTE_ALARM_CODE_DELETE_ALL)) {
			LOGGER.info("Received remote delete all alarms message");
			disableAllAlarmsOfNext24Hours();
			//show the next alarm time on the clock display to verify there is none
			showNextAlarmTime();
		}
		updateAlarmList();
		updateNextAlarmTime();
	}
	
	/**
	 * Show the next alarm time on the clock display (within the next 24 hours only). If there is no alarm the text "NONE" is displayed.
	 */
	public void showNextAlarmTime() {
		LOGGER.debug("Showing next alarm time on clock display");
		Optional<Alarm> nextAlarm = alarmManager.getNextAlarmToPlay();
		boolean alarmFound = false;
		if (nextAlarm.isPresent()) {
			Alarm alarm = nextAlarm.get();
			//check whether the alarm is within the next 24 hours
			if (alarm.getDateTime().isBefore(LocalDateTime.now().plusDays(1))) {
				alarmFound = true;
				//get the alarm time and display it on the clock display
				int hours = alarm.getDateTime().getHour();
				int minutes = alarm.getDateTime().getMinute();
				serialConnection.showTimeForFiveSeconds(hours, minutes);
			}
		}
		
		if (!alarmFound) {
			//no alarm found -> display "None"
			serialConnection.showTimeForFiveSeconds(-1, -1);//shows text "NONE"
		}
	}
	
	public void disableAllAlarms() {
		for (Alarm alarm : getAlarms()) {
			alarm.setActive(false);
		}
	}
	
	public void disableAllAlarmsOfNext24Hours() {
		List<Alarm> next24HourAlarms = alarmManager.getActiveAlarmsForNext24Hours();
		for (Alarm alarm : next24HourAlarms) {
			alarm.setActive(false);
		}
	}
}