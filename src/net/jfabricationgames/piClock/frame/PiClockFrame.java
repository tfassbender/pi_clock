package net.jfabricationgames.piClock.frame;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.jfabricationgames.piClock.clock.ClockManager;
import net.jfabricationgames.piClock.serial.PiClockSerialConnection;
import net.jfabricationgames.piClock.temperature.TemperatureManager;

/**
 * The JavaFX frame for the pi clock project.
 * 
 * This class is deprecated because it doesn't work on the RaspberryPi because the JavaFX library is not available on RaspberryPi.
 */
@Deprecated
public class PiClockFrame extends Application {
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		PiClockSerialConnection serialConnection = new PiClockSerialConnection();
		ClockManager clockManager = new ClockManager(serialConnection);
		TemperatureManager temperatureManager = new TemperatureManager(serialConnection);
		PiClockController controller = new PiClockController();
		clockManager.addTimeChangeListener(controller);
		temperatureManager.addTimeChangeListener(controller);
		
		URL piClockFrameUrl = getClass().getResource("PiClockFrame.fxml");
		FXMLLoader loader = new FXMLLoader(piClockFrameUrl);
		loader.setController(controller);
		Parent root = loader.load();
		
		primaryStage.setScene(new Scene(root, 800, 600));
		primaryStage.setTitle("PiClock");
		primaryStage.getScene().getStylesheets().add(getClass().getResource("PiClockFrame.css").toExternalForm());
		
		primaryStage.setOnCloseRequest(event -> {
			clockManager.stop();
			temperatureManager.stop();
			serialConnection.close();
		});
		
		primaryStage.show();
	}
}