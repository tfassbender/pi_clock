package net.jfabricationgames.piClock.temperature;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.piClock.serial.PiClockSerialConnection;
import net.jfabricationgames.piClock.serial.SerialMessageReceiver;

public class TemperatureManager implements Runnable, SerialMessageReceiver {
	
	private Logger LOGGER = LogManager.getLogger(TemperatureManager.class);
	
	private static final int MAX_TEMPERATURE_VALUES = 10;
	private static final int MAX_HUMIDITY_VALUES = 10;
	
	private static final int RECEIVE_MESSAGE_CAUSE_TEMPERATURE = 1;
	private static final int RECEIVE_MESSAGE_CAUSE_HUMIDITY = 2;
	
	private Thread temperatureManager;
	
	private PiClockSerialConnection piClockConnection;
	
	private List<TemperatureChangeListener> temperatureChangeListeners;
	
	private List<Integer> lastTemperatureValues;
	private List<Integer> lastHumidityValues;
	
	public TemperatureManager(PiClockSerialConnection connection) {
		if (connection == null) {
			throw new IllegalArgumentException("The connection mussn't be null");
		}
		piClockConnection = connection;
		temperatureChangeListeners = new ArrayList<TemperatureChangeListener>();
		lastTemperatureValues = new ArrayList<Integer>(MAX_TEMPERATURE_VALUES+1);
		lastHumidityValues = new ArrayList<Integer>(MAX_HUMIDITY_VALUES+1);
		temperatureManager = new Thread(this, "TemperatureManagerThread");
		temperatureManager.start();
		LOGGER.info("Temperature (-request) thread started"); 
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			getTemperature();
			getHumidity();
			try {
				if (lastTemperatureValues.size() < MAX_TEMPERATURE_VALUES || lastHumidityValues.size() < MAX_HUMIDITY_VALUES) {
					//not enough values in the lists -> only sleep for 10 seconds
					Thread.sleep(10000);
				}
				else {
					//enough values in the lists -> sleep for 1 minute
					Thread.sleep(60000);
				}
			}
			catch (InterruptedException ie) {
				//ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void stop() {
		LOGGER.info("Stopping temperature manager");
		temperatureManager.interrupt();
	}
	
	private void getTemperature() {
		//send a request for the temperature to the controller
		piClockConnection.getTemperature(this, RECEIVE_MESSAGE_CAUSE_TEMPERATURE);
	}
	private void getHumidity() {
		//send a request for the humidity to the controller
		piClockConnection.getHumidity(this, RECEIVE_MESSAGE_CAUSE_HUMIDITY);
	}
	
	private void informListeners() {
		LOGGER.trace("Informing listeners about temperature or humidity change");
		double temperature = 0;
		double humidity = 0;
		for (int t : lastTemperatureValues) {
			temperature += t;
		}
		for (int h : lastHumidityValues) {
			humidity += h;
		}
		temperature /= lastTemperatureValues.size();
		humidity /= lastHumidityValues.size();
		for (TemperatureChangeListener listener : temperatureChangeListeners) {
			listener.temperatureChanged(temperature);
			listener.humidityChanged(humidity);
		}
	}
	
	public void addTemperatureChangeListener(TemperatureChangeListener listener) {
		temperatureChangeListeners.add(listener);
	}
	public boolean removeTemperatureChangeListener(TemperatureChangeListener listener) {
		return temperatureChangeListeners.remove(listener);
	}

	@Override
	public void receiveMessage(String message, int cause) {
		switch (cause) {
			case RECEIVE_MESSAGE_CAUSE_TEMPERATURE:
				try {
					int temperature = Integer.parseInt(message);
					lastTemperatureValues.add(temperature);
					if (lastTemperatureValues.size() > MAX_TEMPERATURE_VALUES) {
						lastTemperatureValues.remove(0);
					}
				}
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				break;
			case RECEIVE_MESSAGE_CAUSE_HUMIDITY:
				try {
					int humidity = Integer.parseInt(message);
					lastHumidityValues.add(humidity);
					if (lastHumidityValues.size() > MAX_HUMIDITY_VALUES) {
						lastHumidityValues.remove(0);
					}
				}
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
				break;
			default:
				throw new IllegalStateException("The given callback cause (" + cause + ") is unknown");
		}
		informListeners();
	}
}