package net.jfabricationgames.piClock.serial;

public class CallbackRequest {
	
	private SerialMessageReceiver callback;
	private int cause;
	
	public CallbackRequest(SerialMessageReceiver callback, int cause) {
		this.callback = callback;
		this.cause = cause;
		if (callback == null) {
			throw new IllegalArgumentException("callback mussn't be null");
		}
	}
	
	public SerialMessageReceiver getCallback() {
		return callback;
	}
	public int getCause() {
		return cause;
	}
}