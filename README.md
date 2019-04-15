# pi_clock

This project was used to build an alarm clock using a RaspberryPi, an Arduino and some electronics.

The principle is that the RaspberryPi runs a java-programm that manages and plays the alarms (any music that is on the SD-Card) while the Arduino (nano) manages the clock display, the displays backlight, an infra-red receiver to turn off the alarm, ...


The RaspberryPi and the arduino use the RXTX lib to communicate via the serial connection (USB)
