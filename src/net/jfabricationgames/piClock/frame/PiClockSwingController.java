package net.jfabricationgames.piClock.frame;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.jfabricationgames.piClock.clock.TimeChangeListener;
import net.jfabricationgames.piClock.temperature.TemperatureChangeListener;

public class PiClockSwingController implements TimeChangeListener, TemperatureChangeListener{
	
	private PiClockFrameSwing frame;
	
	private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
	
	public PiClockSwingController(PiClockFrameSwing frame) {
		this.frame = frame;
	}
	
	@Override
	public void timeChanged(LocalDateTime time) {
		String timeText = time.format(timeFormat);
		if (timeText != null) {
			frame.setTime(timeText);			
		}
	}
	
	@Override
	public void temperatureChanged(double temperature) {
		String temperatureText = String.format("%.1f °C", temperature);
		frame.setTemperature(temperatureText);
	}
	
	@Override
	public void humidityChanged(double humidity) {
		String humidityText = String.format("%.1f", humidity) + " %";
		frame.setHumidity(humidityText);
	}
}