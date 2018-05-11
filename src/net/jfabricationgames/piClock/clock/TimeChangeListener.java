package net.jfabricationgames.piClock.clock;

import java.time.LocalDateTime;

public interface TimeChangeListener {
	
	public void timeChanged(LocalDateTime time);
}