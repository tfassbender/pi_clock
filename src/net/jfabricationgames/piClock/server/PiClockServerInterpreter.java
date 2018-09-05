package net.jfabricationgames.piClock.server;

/**
 * The interpreter for the messages that the {@code PiClockAlarmServer} receives.
 */
@FunctionalInterface
public interface PiClockServerInterpreter {
	
	public void interpreteMessage(AlarmMessage message);
}