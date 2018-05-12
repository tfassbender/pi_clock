package net.jfabricationgames.piClock.serial;

public interface SerialMessageReceiver {
	
	public void receiveMessage(String message, int cause);
}