package net.jfabricationgames.piClock.frame;

import java.io.File;

/**
 * Track is just a Wrapper for a file to display it in the GUI.
 */
public class Track {
	
	private File trackFile;
	
	public Track(File trackFile) {
		this.trackFile = trackFile;
	}
	
	public File getTrackFile() {
		return trackFile;
	}
	
	@Override
	public String toString() {
		String trackName = trackFile.getName();
		return trackName.substring(0, trackName.lastIndexOf("."));
	}
}