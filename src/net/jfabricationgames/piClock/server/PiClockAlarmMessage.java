package net.jfabricationgames.piClock.server;

import java.util.List;
import java.util.Objects;

import net.jfabricationgames.piClock.clock.Alarm;

/**
 * The implementation of the {@code AlarmMessage} interface, used to send (serialized) alarms between the servers.
 */
public class PiClockAlarmMessage implements AlarmMessage {
	
	private AlarmMessage.Type type;
	private List<Alarm> alarmList;
	private boolean active;
	private int id;
	
	public PiClockAlarmMessage(AlarmMessage.Type type, List<Alarm> alarmList, boolean active, int id) {
		Objects.requireNonNull(type);
		this.type = type;
		this.alarmList = alarmList;
		this.active = active;
		this.id = id;
	}
	public PiClockAlarmMessage(AlarmMessage.Type type, List<Alarm> alarmList) {
		this(type, alarmList, false, -1);
	}
	public PiClockAlarmMessage(AlarmMessage.Type type, boolean active, int id) {
		this(type, null, active, id);
	}
	public PiClockAlarmMessage(AlarmMessage.Type type) {
		this(type, null, false, -1);
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public List<Alarm> getAlarmList() {
		return alarmList;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public int getId() {
		return id;
	}
}