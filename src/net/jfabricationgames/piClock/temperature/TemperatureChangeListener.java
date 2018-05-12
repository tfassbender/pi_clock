package net.jfabricationgames.piClock.temperature;

public interface TemperatureChangeListener {
	
	public void temperatureChanged(double temperature);
	
	public void humidityChanged(double humidity);
}