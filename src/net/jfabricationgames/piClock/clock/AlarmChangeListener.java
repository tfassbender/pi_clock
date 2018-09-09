package net.jfabricationgames.piClock.clock;

public interface AlarmChangeListener {
	
	public void addAlarmRemote(Alarm alarm);
	public void removeAlarmRemote(Alarm alarm);
	public void setAlarmActiveRemote(int id, boolean active);
}