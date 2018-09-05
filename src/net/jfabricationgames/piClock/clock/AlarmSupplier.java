package net.jfabricationgames.piClock.clock;

import java.util.List;

public interface AlarmSupplier {
	
	public List<Alarm> loadAlarms();
	
	public void addAlarm(Alarm alarm);
	
	public void removeAlarm(Alarm alarm);
}