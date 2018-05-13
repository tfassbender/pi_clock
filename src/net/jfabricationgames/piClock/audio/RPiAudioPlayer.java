package net.jfabricationgames.piClock.audio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class RPiAudioPlayer {
	
	private boolean trackPaused;
	
	private List<File> tracks;
	
	private Properties trackProperties;
	
	private Process player = null;
	
	private BufferedReader playerOut = null;
	private BufferedReader playerErr = null;
	private BufferedWriter playerIn = null;
	
	private static final String TRACK_PROPERTIES_DIR = ".pi_clock_properties";
	private static final String TRACK_PROPERTIES_PATH = TRACK_PROPERTIES_DIR + "/tracks.properties";
	private static final String TRACK_DIR_PROPERTY = "track_dir";
	private static final String DEFAULT_TRACK_DIR = "tracks";
	private static final String PLAYER_COMMAND = "omxplayer -o local ";
	
	public RPiAudioPlayer() throws IOException {
		trackProperties = new Properties();
		try (InputStream inStream = new FileInputStream(new File(TRACK_PROPERTIES_PATH))) {
			//load the properties from the file
			trackProperties.load(inStream);
			String trackDir = trackProperties.getProperty(TRACK_DIR_PROPERTY);
			if (trackDir == null) {
				//add the property if it isn't found in the file
				trackProperties.setProperty(TRACK_DIR_PROPERTY, DEFAULT_TRACK_DIR);
				try (OutputStream outStream = new FileOutputStream(new File(TRACK_PROPERTIES_PATH))) {
					trackProperties.store(outStream, "set the path to the tracks, that are to be played as alarm, here");
				}
				catch (IOException ioe2) {
					throw new IOException("Couldn't create a properties file.", ioe2); 
				}
			}
			else {
				loadTracks(trackDir);
			}
		}
		catch (IOException ioe) {
			System.err.println("Couldn't open properties file. Creating default file.");
			//try to create the properties file if there is none
			trackProperties.setProperty(TRACK_DIR_PROPERTY, DEFAULT_TRACK_DIR);
			File dir = new File(TRACK_PROPERTIES_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			try (OutputStream outStream = new FileOutputStream(new File(TRACK_PROPERTIES_PATH))) {
				trackProperties.store(outStream, "set the path to the tracks, that are to be played as alarm, here");
			}
			catch (IOException ioe2) {
				throw new IOException("Couldn't create a properties file.", ioe2); 
			}
			loadTracks(DEFAULT_TRACK_DIR);
		}
	}
	
	private void loadTracks(String trackDir) throws IOException {
		File dir = new File(trackDir);
		if (dir.exists() && dir.isDirectory()) {
			tracks = Arrays.stream(dir.listFiles()).filter(file -> file.getName().endsWith(".mp3")).collect(Collectors.toList());
			if (tracks.isEmpty()) {
				throw new IOException("The track directory doesn't contain any '.mp3' files");
			}
		}
		else {
			throw new IOException("The chosen track directory doesn't exist or is no directory (chosen dir: " + trackDir + "");
		}
	}
	
	public void createPlayer() throws IOException {
		if (player != null) {
			player.destroy();
			try {
				playerOut.close();
				playerErr.close();
				playerIn.close();
			}
			catch (IOException ioe) {
				//ioe.printStackTrace();
			}
		}
		int randomTrackNumber = (int) (Math.random() * tracks.size());
		File playedTrack = tracks.get(randomTrackNumber);
		player = Runtime.getRuntime().exec(PLAYER_COMMAND + playedTrack.getAbsolutePath());
		playerOut = new BufferedReader(new InputStreamReader(player.getInputStream()));
		playerErr = new BufferedReader(new InputStreamReader(player.getErrorStream()));
		playerIn = new BufferedWriter(new OutputStreamWriter(player.getOutputStream()));
		trackPaused = false;
	}
	
	public void play() throws IOException {
		if (player == null) {
			//create a new player and play a random track
			createPlayer();
		}
		else {
			if (trackPaused) {
				//if the track is paused, start it again
				playerIn.write("p");
				playerIn.flush();
				trackPaused = false;
			}
			else {
				//check whether the last track has ended or has stopped for any other reason
				if (!player.isAlive()) {
					createPlayer();
				}
				//else: when the player is already playing, do nothing
			}
		}
	}
	
	public void pause() {
		if (player != null) {
			try {
				playerIn.write("p");
				playerIn.flush();
				trackPaused = true;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public void stop() throws IllegalStateException {
		if (player != null) {
			try {
				playerIn.write("q");
				playerIn.flush();
				trackPaused = false;
				Thread.sleep(50);//give the player a short time to stop
				if (player.isAlive()) {
					//if the player doesn't stop in the given time -> destroy the process
					player.destroy();
				}
				//close the streams
				playerIn.close();
				playerOut.close();
				playerErr.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			catch (InterruptedException ie) {
				//ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
			finally {
				if (player != null && player.isAlive()) {
					//if the player is still alive try to destroy it
					player.destroy();
				}
				player = null;
				playerIn = null;
				playerOut = null;
				playerErr = null;
			}
		}
		else {
			throw new IllegalStateException("There is no player that could be stopped at the moment.");
		}
	}
	
	public void nextTrack() throws IOException {
		try {
			stop();
			play();
		}
		catch (IllegalStateException ie) {
			//ignore this exception that just says that there was no track playing
		}
	}
	
	public void increaseVolume() throws IllegalStateException {
		if (player != null) {
			try {
				playerIn.write("+");
				playerIn.flush();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	public void decreaseVolume() throws IllegalStateException {
		if (player != null) {
			try {
				playerIn.write("-");
				playerIn.flush();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}