package net.jfabricationgames.piClock.clock;

import java.util.List;

/**
 * Supply the alarms to the {@code AlarmManager} by loading them from the database (and using the {@code PiClockLocalServer} for push-messages).
 */
public class DatabaseAlarmStore implements AlarmSupplier {
	
	@Override
	public List<Alarm> loadAlarms() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addAlarm(Alarm alarm) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeAlarm(Alarm alarm) {
		// TODO Auto-generated method stub
		
	}
}