package net.jfabricationgames.piClock.frame;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import net.jfabricationgames.piClock.clock.TimeChangeListener;
import net.jfabricationgames.piClock.temperature.TemperatureChangeListener;

public class PiClockController implements Initializable, TimeChangeListener, TemperatureChangeListener {
	
	@FXML
	private Label labelClock;
	@FXML
	private Label labelTemperature;
	@FXML
	private Label labelHumidity;
	
	private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}
	
	@Override
	public void timeChanged(LocalDateTime time) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String timeText = time.format(timeFormat);
				labelClock.setText(timeText);
			}
		});
	}
	
	@Override
	public void temperatureChanged(double temperature) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String temperatureText = String.format("%.1f °C", temperature);
				labelTemperature.setText(temperatureText);
			}
		});
	}
	
	@Override
	public void humidityChanged(double humidity) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String humidityText = String.format("%.1f", humidity) + " %";
				labelHumidity.setText(humidityText);
			}
		});
	}
}