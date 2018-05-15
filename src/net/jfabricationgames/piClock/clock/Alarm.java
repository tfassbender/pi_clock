package net.jfabricationgames.piClock.clock;

import java.io.Serializable;
import java.time.LocalDateTime;

public interface Alarm extends TimeChangeListener, Serializable, Comparable<Alarm> {
	
	public boolean isPlaying();
	
	public boolean isActive();
	
	public void setActive(boolean active);
	
	public LocalDateTime getDateTime();
	
	public AlarmRepetition getRepetition();
	
	public void stop();
	
	public void pause(int seconds);
	
	public boolean isPaused();
}