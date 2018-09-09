package net.jfabricationgames.piClock.server;

import java.util.List;

import net.jfabricationgames.piClock.clock.Alarm;

public interface AlarmMessage {
	
	public static enum Type {
		ADD,		//new alarm via push
		REMOVE,		//remove alarm via push
		SET_ACTIVE,	//set alarm active state via push
		LOAD_ALL;	//require (client) or send (server) all alarms
	}
	
	public Type getType();
	public List<Alarm> getAlarmList();
	
	//when the active state is changed only the id and the active flag are sent
	public boolean isActive();
	public int getId();
}