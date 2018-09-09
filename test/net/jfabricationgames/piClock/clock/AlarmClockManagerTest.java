package net.jfabricationgames.piClock.clock;

import static org.junit.Assert.assertFalse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import net.jfabricationgames.piClock.frame.PiClockFrameSwing;
import net.jfabricationgames.piClock.frame.PiClockSwingController;

/**
 * Test the serialization of the alarms.
 * 
 * This test sometimes needs to be run twice to work, because the load can be called before the write.
 */
@Ignore
public class AlarmClockManagerTest {
	
	private static PiClockSwingController controller;
	private static AlarmClockManager manager;
	
	@BeforeClass
	public static void initManager() {
		PiClockFrameSwing frame = new PiClockFrameSwing();
		controller = frame.getController();
		manager = controller.getAlarmManager();
	}
	
	@After
	public void closeAll() {
		controller.stopAll();
	}
	
	@Test
	public void testStoreAlarms() {
		PiClockAlarm alarm = new PiClockAlarm(LocalDateTime.now(), manager, true, AlarmRepetition.NONE);
		manager.addAlarm(alarm);
		alarm = new PiClockAlarm(LocalDateTime.now().plus(10, ChronoUnit.DAYS), manager, true, AlarmRepetition.DAILY);
		manager.addAlarm(alarm);
		manager.storeAlarms();
	}
	
	/**
	 * The test won't work because in the new version the alarms are stored in a database
	 */
	@Test
	public void testLoadAlarms() {
		//manager.loadAlarms();
		assertFalse(manager.getAlarms().isEmpty());
	}
}