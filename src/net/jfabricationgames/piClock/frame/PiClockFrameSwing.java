package net.jfabricationgames.piClock.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.jfabricationgames.piClock.audio.RPiAudioPlayer;
import net.jfabricationgames.piClock.clock.Alarm;
import net.jfabricationgames.piClock.clock.AlarmRepetition;
import net.miginfocom.swing.MigLayout;

public class PiClockFrameSwing extends JFrame {

	private static final long serialVersionUID = -1567530055356287961L;
	
	private JPanel contentPane;
	private JLabel lblTime_1;
	private JLabel lblTemperature_1;
	private JLabel lblHumidity_1;
	
	private PiClockSwingController controller;
	private JSpinner spinnerHour;
	private JSpinner spinnerMinute;
	private JComboBox<AlarmRepetition> comboBox;
	
	private Alarm selectedAlarm;
	private JCheckBox chckbxAlarmActive;
	private JList<Alarm> listAlarms;
	
	private DefaultListModel<Alarm> alarmListModel;
	
	private Thread spinnerButtonThread;
	private JLabel lblNextAlarmTime;
	private JSlider slider;
	private JSpinner spinner;
	
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
		controller = new PiClockSwingController(this);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				controller.stopAll();
				controller.storeAlarms();
			}
		});
		
		setTitle("PiClock");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 450);
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
		panelAlarm.setLayout(new MigLayout("", "[50px][50px][30px][30px][10px][][100px,grow][100px]", "[][grow][][center][center][][10px][][10px][]"));
		
		JLabel lblAlarms = new JLabel("Alarms:");
		lblAlarms.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelAlarm.add(lblAlarms, "cell 5 0 2 1");
		
		JButton btnPauseAlarm = new JButton("Pause Alarm");
		btnPauseAlarm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.pauseAlarm();
			}
		});
		btnPauseAlarm.setFont(new Font("Tahoma", Font.BOLD, 35));
		panelAlarm.add(btnPauseAlarm, "cell 0 0 4 2,alignx center");
		
		chckbxAlarmActive = new JCheckBox("Alarm active");
		chckbxAlarmActive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedAlarm != null) {
					selectedAlarm.setActive(chckbxAlarmActive.isSelected());
					updateAlarmList();
					updateNextAlarmTime();
				}
			}
		});
		chckbxAlarmActive.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelAlarm.add(chckbxAlarmActive, "cell 7 0,alignx right");
		
		JScrollPane scrollPane = new JScrollPane();
		panelAlarm.add(scrollPane, "cell 5 1 3 4,grow");
		
		alarmListModel = new DefaultListModel<Alarm>();
		listAlarms = new JList<Alarm>(alarmListModel);
		listAlarms.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectedAlarm = listAlarms.getSelectedValue();
				chckbxAlarmActive.setSelected(selectedAlarm != null && selectedAlarm.isActive());
			}
		});
		listAlarms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAlarms.setBackground(Color.LIGHT_GRAY);
		listAlarms.setFont(new Font("Tahoma", Font.BOLD, 18));
		scrollPane.setViewportView(listAlarms);
		
		JLabel lblNewAlarm = new JLabel("New Alarm:");
		lblNewAlarm.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelAlarm.add(lblNewAlarm, "cell 0 2 4 1");
		
		JLabel lblHour = new JLabel("Hour:");
		lblHour.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelAlarm.add(lblHour, "cell 0 3,aligny center");
		
		spinnerHour = new JSpinner();
		spinnerHour.setFont(new Font("Tahoma", Font.BOLD, 16));
		spinnerHour.setModel(new SpinnerNumberModel(7, 0, 23, 1));
		panelAlarm.add(spinnerHour, "cell 1 3,growx");
		
		JButton btn_increase_hour = new JButton("+");
		btn_increase_hour.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				startSpinnerButtonThread(() -> increaseHour());
			}
		});
		btn_increase_hour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerButtonThread.interrupt();
			}
		});
		btn_increase_hour.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelAlarm.add(btn_increase_hour, "cell 2 3");
		
		JButton btn_decrease_hour = new JButton("-");
		btn_decrease_hour.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startSpinnerButtonThread(() -> decreaseHour());
			}
		});
		btn_decrease_hour.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerButtonThread.interrupt();
			}
		});
		btn_decrease_hour.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelAlarm.add(btn_decrease_hour, "cell 3 3");
		
		JButton btn_increase_minute = new JButton("+");
		btn_increase_minute.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startSpinnerButtonThread(() -> increaseMinute());
			}
		});
		btn_increase_minute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerButtonThread.interrupt();
			}
		});
		btn_increase_minute.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelAlarm.add(btn_increase_minute, "cell 2 4");
		
		JButton btn_decrease_minute = new JButton("-");
		btn_decrease_minute.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				startSpinnerButtonThread(() -> decreaseMinute());
			}
		});
		btn_decrease_minute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinnerButtonThread.interrupt();
			}
		});
		btn_decrease_minute.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelAlarm.add(btn_decrease_minute, "cell 3 4");
		
		JLabel lblNextAlarmIn = new JLabel("Next Alarm in:");
		lblNextAlarmIn.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelAlarm.add(lblNextAlarmIn, "cell 5 5,aligny center");
		
		lblNextAlarmTime = new JLabel("");
		lblNextAlarmTime.setForeground(Color.RED);
		lblNextAlarmTime.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelAlarm.add(lblNextAlarmTime, "cell 6 5 2 1,alignx center,aligny center");
		
		JButton btnAlarmOff = new JButton("Alarm Off");
		btnAlarmOff.setForeground(Color.RED);
		btnAlarmOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.stopAlarm();
			}
		});
		btnAlarmOff.setFont(new Font("Tahoma", Font.BOLD, 40));
		panelAlarm.add(btnAlarmOff, "cell 5 7 3 3,alignx center");
		
		JLabel lblMinute = new JLabel("Minute:");
		lblMinute.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelAlarm.add(lblMinute, "cell 0 4,aligny center");
		
		spinnerMinute = new JSpinner();
		spinnerMinute.setFont(new Font("Tahoma", Font.BOLD, 16));
		spinnerMinute.setModel(new SpinnerNumberModel(0, 0, 59, 1));
		panelAlarm.add(spinnerMinute, "cell 1 4,growx");
		
		JLabel lblRepeat = new JLabel("Repeat:");
		lblRepeat.setFont(new Font("Tahoma", Font.BOLD, 16));
		panelAlarm.add(lblRepeat, "cell 0 5,alignx trailing,aligny center");
		
		DefaultComboBoxModel<AlarmRepetition> comboBoxModel = new DefaultComboBoxModel<AlarmRepetition>();
		for (AlarmRepetition repetition : AlarmRepetition.values()) {
			comboBoxModel.addElement(repetition);
		}
		comboBox = new JComboBox<AlarmRepetition>(comboBoxModel);
		comboBox.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panelAlarm.add(comboBox, "cell 1 5 3 1,growx");
		
		JButton btnAddAlarm = new JButton("Add Alarm");
		btnAddAlarm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.addAlarm((Integer) spinnerHour.getValue(), (Integer) spinnerMinute.getValue(), 
						(AlarmRepetition) comboBox.getSelectedItem());
				updateAlarmList();
				updateNextAlarmTime();
			}
		});
		btnAddAlarm.setFont(new Font("Tahoma", Font.BOLD, 25));
		panelAlarm.add(btnAddAlarm, "cell 0 7 4 1,alignx center,aligny top");
		
		JButton btnRemoveAlarm = new JButton("Remove Alarm");
		btnRemoveAlarm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedAlarm != null) {
					controller.removeAlarm(selectedAlarm);
				}
				updateAlarmList();
				updateNextAlarmTime();
			}
		});
		btnRemoveAlarm.setFont(new Font("Tahoma", Font.BOLD, 25));
		panelAlarm.add(btnRemoveAlarm, "cell 0 9 4 1,alignx center");
		
		JPanel panelPlayer = new JPanel();
		tabbedPane.addTab("Player", null, panelPlayer, null);
		panelPlayer.setLayout(new MigLayout("", "[10px][20px][10px][30px][50px][][][30px][][grow]", "[][][][][][20px][][][][grow]"));
		
		JLabel lblVolume = new JLabel("Volume:");
		lblVolume.setFont(new Font("Tahoma", Font.BOLD, 24));
		panelPlayer.add(lblVolume, "cell 0 0 8 1");
		
		JLabel lblScilence = new JLabel("I want to sleep...");
		lblScilence.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelPlayer.add(lblScilence, "cell 0 1 3 1");
		
		slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateVolume(true, false);
			}
		});
		slider.setMaximum(10);
		slider.setValue(RPiAudioPlayer.INITIAL_VOLUME);
		panelPlayer.add(slider, "cell 3 1 5 1");
		
		JLabel lblLounderThanHell = new JLabel("Wake up already!!!");
		lblLounderThanHell.setFont(new Font("Tahoma", Font.BOLD, 18));
		panelPlayer.add(lblLounderThanHell, "cell 8 1");
		
		spinner = new JSpinner();
		spinner.setFont(new Font("Tahoma", Font.BOLD, 18));
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateVolume(false, true);
			}
		});
		spinner.setModel(new SpinnerNumberModel(RPiAudioPlayer.INITIAL_VOLUME, 0, 10, 1));
		panelPlayer.add(spinner, "cell 4 2,growx,aligny center");
		
		JButton button = new JButton("+");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				increaseVolume();
			}
		});
		button.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelPlayer.add(button, "cell 5 2");
		
		JButton button_1 = new JButton("-");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				decreaseVolume();
			}
		});
		button_1.setFont(new Font("Tahoma", Font.BOLD, 30));
		panelPlayer.add(button_1, "cell 6 2");
		
		JLabel lblTracklist = new JLabel("Tracklist:");
		lblTracklist.setFont(new Font("Tahoma", Font.BOLD, 24));
		panelPlayer.add(lblTracklist, "cell 0 4 3 1");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panelPlayer.add(scrollPane_1, "cell 3 4 7 6,grow");
		
		JList<String> list = new JList<String>();
		list.setBackground(Color.LIGHT_GRAY);
		list.setFont(new Font("Tahoma", Font.BOLD, 18));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane_1.setViewportView(list);
		
		JButton btnPlay = new JButton("Play");
		btnPlay.setFont(new Font("Tahoma", Font.BOLD, 25));
		panelPlayer.add(btnPlay, "cell 1 6,growx");
		
		JButton btnPause = new JButton("Pause");
		btnPause.setFont(new Font("Tahoma", Font.BOLD, 25));
		panelPlayer.add(btnPause, "cell 1 7,growx");
		
		JButton btnStop = new JButton("Stop");
		btnStop.setFont(new Font("Tahoma", Font.BOLD, 25));
		panelPlayer.add(btnStop, "cell 1 8,growx");
	}
	
	private void updateVolume(boolean sliderCalling, boolean spinnerCalling) {
		int volume;
		if (spinner != null) {
			if (sliderCalling) {
				volume = slider.getValue();
				spinner.setValue(volume);
			}
			else {
				//spinner (or indirect buttons) calling
				volume = (Integer) spinner.getValue();
				slider.setValue(volume);
			}
			controller.setPlayerVolume(volume);			
		}
	}
	
	private void increaseVolume() {
		int volume = slider.getValue();
		volume++;
		volume = Math.max(volume, 0);
		volume = Math.min(volume, 10);
		slider.setValue(volume);
	}
	private void decreaseVolume() {
		int volume = slider.getValue();
		volume--;
		volume = Math.max(volume, 0);
		volume = Math.min(volume, 10);
		slider.setValue(volume);
	}
	
	private void increaseHour() {
		int hour = (Integer) spinnerHour.getValue();
		hour++;
		if (hour > 23) {
			hour = 0;
		}
		if (hour < 0) {
			hour = 23;
		}
		hour = Math.max(hour, 0);
		hour = Math.min(hour, 23);
		spinnerHour.setValue(hour);
	}
	private void decreaseHour() {
		int hour = (Integer) spinnerHour.getValue();
		hour--;
		if (hour > 23) {
			hour = 0;
		}
		if (hour < 0) {
			hour = 23;
		}
		hour = Math.max(hour, 0);
		hour = Math.min(hour, 23);
		spinnerHour.setValue(hour);
	}
	private void increaseMinute() {
		int minute = (Integer) spinnerMinute.getValue();
		minute++;
		if (minute > 59) {
			minute = 0;
		}
		if (minute < 0) {
			minute = 59;
		}
		minute = Math.max(minute, 0);
		minute = Math.min(minute, 59);
		spinnerMinute.setValue(minute);
	}
	private void decreaseMinute() {
		int minute = (Integer) spinnerMinute.getValue();
		minute--;
		if (minute > 59) {
			minute = 0;
		}
		if (minute < 0) {
			minute = 59;
		}
		minute = Math.max(minute, 0);
		minute = Math.min(minute, 59);
		spinnerMinute.setValue(minute);
	}
	
	private void startSpinnerButtonThread(Runnable spinnerChange) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				//run the spinner change and wait 500ms the first time
				try {
					spinnerChange.run();
					Thread.sleep(500);
				}
				catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				while (!Thread.currentThread().isInterrupted()) {
					//after the first wait only wait for 100ms per increase
					try {
						spinnerChange.run();
						Thread.sleep(100);
					}
					catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};
		if (spinnerButtonThread != null) {
			spinnerButtonThread.interrupt();
		}
		spinnerButtonThread = new Thread(runnable, "SpinnerButtonThread");
		spinnerButtonThread.setPriority(Thread.MIN_PRIORITY);
		spinnerButtonThread.setDaemon(true);
		spinnerButtonThread.start();
	}
	
	public void updateAlarmList() {
		if (alarmListModel != null) {
			alarmListModel.removeAllElements();
			for (Alarm alarm : controller.getAlarms()) {
				alarmListModel.addElement(alarm);
			}
			repaint();			
		}
	}
	private void updateNextAlarmTime() {
		controller.updateNextAlarmTime();
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
	
	public void setTimeTillNextAlarm(int hours, int minutes) {
		if (lblNextAlarmTime != null) {
			if (hours == -1 || minutes == -1) {
				lblNextAlarmTime.setText("--:--");
			}
			else {
				StringBuilder sb = new StringBuilder();
				if (hours < 10) {
					sb.append('0');
				}
				sb.append(Integer.toString(hours));
				sb.append(':');
				if (minutes < 10) {
					sb.append('0');
				}
				sb.append(minutes);
				lblNextAlarmTime.setText(sb.toString());
			}			
		}
	}
	
	public PiClockSwingController getController() {
		return controller;
	}
}