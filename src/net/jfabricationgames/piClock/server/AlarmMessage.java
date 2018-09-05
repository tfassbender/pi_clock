package net.jfabricationgames.piClock.server;

import java.util.List;

import net.jfabricationgames.piClock.clock.Alarm;

public interface AlarmMessage {
	
	public static enum Type {
		ADD,		//new alarm via push
		REMOVE,		//remove alarm via push
		ACTIVE,		//set alarm active via push
		NOT_ACTIVE,	//set alarm in-active via push
		LOAD_ALL;	//require (client) or send (server) all alarms
	}
	
	public Type getType();
	public List<Alarm> getAlarmList();
}