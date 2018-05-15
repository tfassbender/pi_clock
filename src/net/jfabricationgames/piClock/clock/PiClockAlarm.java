package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class PiClockAlarm implements Alarm {
	
	private static final long serialVersionUID = -1312956845922157557L;
	
	private AlarmRepetition repetition;
	private LocalDateTime alarmDateTime;
	private boolean active;
	private transient boolean paused;
	private transient AlarmClockManager manager;
	
	private transient DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm (EEE)");
	
	public PiClockAlarm(LocalDateTime alarmDateTime, AlarmClockManager manager, boolean active, AlarmRepetition repetition) {
		Objects.requireNonNull(alarmDateTime, "You can't create an alarm without a date and time.");
		Objects.requireNonNull(manager, "You need an AlarmClockManager to manage the alarm.");
		Objects.requireNonNull(repetition, "You need to specify a repetition (use AlarmRepetition.NONE if you don't want to repeat).");
		this.alarmDateTime = alarmDateTime;
		this.manager = manager;
		this.active = active;
		this.repetition = repetition;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PiClockAlarm) {
			PiClockAlarm alarm = (PiClockAlarm) obj;
			return active == alarm.active && repetition.equals(alarm.repetition) && alarmDateTime.equals(alarm.alarmDateTime);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Alarm: ");
		sb.append(alarmDateTime.format(timeFormat));
		sb.append(" (");
		if (active) {
			sb.append("active)");
		}
		else {
			sb.append("not active)");
		}
		return sb.toString();
	}
	
	@Override
	public boolean isPlaying() {
		return manager.isAlarmPlaying(this);
	}
	@Override
	public boolean isActive() {
		return active;
	}
	@Override
	public void setActive(boolean active) {
		this.active = active;
		if (active && alarmDateTime.isBefore(LocalDateTime.now())) {
			//when the alarm is activated but lies in the past make it move to the future so it doesn't run immediately
			if (repetition == AlarmRepetition.WEEKLY) {
				long weeksTillNow = alarmDateTime.until(LocalDateTime.now(), ChronoUnit.WEEKS);
				alarmDateTime = alarmDateTime.plus(weeksTillNow + 1, ChronoUnit.WEEKS);
			}
			else {
				//daily or none are handled the same (both as daily)
				long daysTillNow = alarmDateTime.until(LocalDateTime.now(), ChronoUnit.DAYS);
				alarmDateTime = alarmDateTime.plus(daysTillNow + 1, ChronoUnit.DAYS);
			}
		}
	}
	@Override
	public LocalDateTime getDateTime() {
		return alarmDateTime;
	}
	@Override
	public AlarmRepetition getRepetition() {
		return repetition;
	}
	@Override
	public void stop() {
		manager.stopAlarm(this);
	}
	@Override
	public void pause(int seconds) {
		paused = manager.pauseAlarm(this);
		if (paused) {
			Thread resumerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(seconds*1000);
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					resumeAlarm();
				}
			}, "AlarmResumeThread");
			resumerThread.setPriority(Thread.MIN_PRIORITY);
			resumerThread.setDaemon(true);
			resumerThread.start();			
		}
		else {
			throw new IllegalStateException("Couldn't pause the alarm.");
		}
	}
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	private void resumeAlarm() {
		if (paused) {
			if (manager.playAlarm(this)) {
				paused = false;
			}
			else {
				throw new IllegalStateException("Couldn't resume the alarm playing.");
			}
		}
	}
	
	@Override
	public void timeChanged(LocalDateTime time) {
		if (active) {
			if (time.isEqual(alarmDateTime) || time.isAfter(alarmDateTime)) {
				manager.playAlarm(this);
				switch (repetition) {
					case NONE:
						active = false;
						break;
					case DAILY:
						active = true;
						alarmDateTime = alarmDateTime.plusDays(1);
						break;
					case WEEKLY:
						active = true;
						alarmDateTime = alarmDateTime.plusDays(7);
						break;
				}
			}
		}
	}

	@Override
	public int compareTo(Alarm alarm) {
		if (active == alarm.isActive()) {
			if (alarmDateTime.isEqual(alarm.getDateTime())) {
				return 0;
			}
			else if (alarmDateTime.isBefore(alarm.getDateTime())) {
				return -1;
			}
			else {
				return 1;
			}
		}
		else {
			if (active) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}
}