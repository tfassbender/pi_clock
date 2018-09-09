package net.jfabricationgames.piClock.clock;

import java.util.List;

public interface AlarmSupplier {
	
	public List<Alarm> loadAlarms();
	
	public void storeAlarms();
	
	public void addAlarm(Alarm alarm);
	
	public void removeAlarm(Alarm alarm);
	
	public void addAlarmRemote(Alarm alarm);
	
	public void removeAlarmRemote(Alarm alarm);
	
	public void addAlarmChangeListener(AlarmChangeListener listener);
	
	public void removeAlarmChangeListener(AlarmChangeListener listener);
}