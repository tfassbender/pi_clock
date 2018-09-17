package net.jfabricationgames.piClock.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for the server implementation.
 * 
 * Creates and starts the server.
 */
public class PiClockAlarmServerMain {
	
	private final Logger LOGGER = LogManager.getLogger(PiClockAlarmServerMain.class);
	
	public static void main(String[] args) {
		new PiClockAlarmServerMain(args);
	}
	
	public PiClockAlarmServerMain(String[] args) {
		if (args.length == 1) {
			
			String clockLocalServerHost = args[0];
			
			//test whether the PiClock is reachable
			try {
				InetAddress piClockAddress = InetAddress.getByName(clockLocalServerHost);
				
				if (piClockAddress.isReachable(5000)) {
					
					//start the server if the PiClock-Local-Server can be reached
					PiClockAlarmServer server = new PiClockAlarmServer(clockLocalServerHost);
					try {
						server.startServer();
					}
					catch (IOException ioe) {
						LOGGER.error("An error occured while trying to start the server", ioe);
						ioe.printStackTrace();
					}
				}
				else {
					throw new UnknownHostException("Could not reach host in time: " + clockLocalServerHost);
				}
			}
			catch (IOException e) {
				LOGGER.error("Could not reach the given PiClock-Local-Server using the given host address: {}", clockLocalServerHost, e);
				System.err.println("Could not reach the given PiClock-Local-Server using the given host address: " + clockLocalServerHost
						+ "\nExiting programm");
				e.printStackTrace();
				System.exit(1);
			}
		}
		else {
			LOGGER.error(
					"The programm could not be started because of the invalid number of starting parameters (the hostname of the PiClock is expected)");
			System.err.println(
					"Wrong number of starting parameters. Please start the programm with the right starting parameters (the hostname of the PiClock)");
			System.exit(1);
		}
	}
}