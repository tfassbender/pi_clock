package net.jfabricationgames.piClock.frame;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import net.jfabricationgames.piClock.clock.ClockManager;
import net.jfabricationgames.piClock.serial.PiClockSerialConnection;
import net.jfabricationgames.piClock.temperature.TemperatureManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PiClockFrameSwing extends JFrame {

	private static final long serialVersionUID = -1567530055356287961L;
	
	private JPanel contentPane;
	private JLabel lblTime_1;
	private JLabel lblTemperature_1;
	private JLabel lblHumidity_1;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PiClockFrameSwing frame = new PiClockFrameSwing();
					frame.setVisible(true);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public PiClockFrameSwing() {
		PiClockSerialConnection serialConnection = new PiClockSerialConnection();
		ClockManager clockManager = new ClockManager(serialConnection);
		TemperatureManager temperatureManager = new TemperatureManager(serialConnection);
		PiClockSwingController controller = new PiClockSwingController(this);
		clockManager.addTimeChangeListener(controller);
		temperatureManager.addTimeChangeListener(controller);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				clockManager.stop();
				temperatureManager.stop();
				serialConnection.close();
			}
		});
		
		setTitle("PiClock");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 350);
		contentPane = new JPanel();
		contentPane.setBackground(Color.GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelInfo = new JPanel();
		tabbedPane.addTab("Info", null, panelInfo, null);
		panelInfo.setLayout(new MigLayout("", "[][grow]", "[][50px][][50px][][50px][grow]"));
		
		JLabel lblTime = new JLabel("Time:");
		lblTime.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelInfo.add(lblTime, "cell 0 0");
		
		lblTime_1 = new JLabel("");
		lblTime_1.setForeground(Color.RED);
		lblTime_1.setFont(new Font("Tahoma", Font.BOLD, 36));
		panelInfo.add(lblTime_1, "cell 0 1");
		
		JLabel lblTemperature = new JLabel("Temperature:");
		lblTemperature.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelInfo.add(lblTemperature, "cell 0 2");
		
		lblTemperature_1 = new JLabel("");
		lblTemperature_1.setForeground(Color.RED);
		lblTemperature_1.setFont(new Font("Tahoma", Font.BOLD, 36));
		panelInfo.add(lblTemperature_1, "cell 0 3");
		
		JLabel lblHumidity = new JLabel("Humidity:");
		lblHumidity.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelInfo.add(lblHumidity, "cell 0 4");
		
		lblHumidity_1 = new JLabel("");
		lblHumidity_1.setForeground(Color.RED);
		lblHumidity_1.setFont(new Font("Tahoma", Font.BOLD, 36));
		panelInfo.add(lblHumidity_1, "cell 0 5");
		
		JPanel panelAlarm = new JPanel();
		tabbedPane.addTab("Alarm", null, panelAlarm, null);
		panelAlarm.setLayout(new MigLayout("", "[]", "[]"));
	}
	
	public void setTime(String time) {
		if (lblTime_1 != null) {
			lblTime_1.setText(time);			
		}
	}
	public void setTemperature(String temperature) {
		lblTemperature_1.setText(temperature);
	}
	public void setHumidity(String humidity) {
		lblHumidity_1.setText(humidity);
	}
}