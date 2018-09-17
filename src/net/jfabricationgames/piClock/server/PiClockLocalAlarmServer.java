package net.jfabricationgames.piClock.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.clock.Alarm;
import net.jfabricationgames.piClock.clock.DatabaseAlarmStore;

/**
 * The local server that is used to receive push-message alarms.
 */
public class PiClockLocalAlarmServer extends PiClockAlarmServer {
	
	public static final int PORT = 4711;
	
	private final Logger LOGGER = LogManager.getLogger(PiClockLocalAlarmServer.class);
	
	private DatabaseAlarmStore alarmStore;
	
	public PiClockLocalAlarmServer(DatabaseAlarmStore alarmStore) {
		super();
		this.alarmStore = alarmStore;
	}
	
	@Override
	protected PiClockServerInterpreter getInterpreterInstance() {
		return (message, connection) -> {
			switch (message.getType()) {
				case ADD:
					if (message.getAlarmList() != null && !message.getAlarmList().isEmpty()) {
						Alarm alarm = message.getAlarmList().get(0);
						alarmStore.addAlarmRemote(alarm);
					}
					else {
						LOGGER.warn("Received a add-alarm-message but with no new alarm");
					}
					break;
				case REMOVE:
					alarmStore.removeAlarmRemote(message.getId());
					break;
				case SET_ACTIVE:
					alarmStore.setAlarmActiveRemote(message.getId(), message.isActive());
					break;
				case ACK:
				case LOAD_ALL:
				default:
					LOGGER.warn("Interpreter received a message with unknown or unexpected type: {}", message.getType());
					break;
				
			}
		};
	}
}