package net.jfabricationgames.piClock.clock;

public interface AlarmChangeListener {
	
	public void addAlarmRemote(Alarm alarm);
	public void removeAlarmRemote(int id);
	public void setAlarmActiveRemote(int id, boolean active);
}