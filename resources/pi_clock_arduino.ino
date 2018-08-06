#include <Arduino.h>
#include <TM1637Display.h>

// Module connection pins (Digital Pins)
#define CLK 3
#define DIO 2

// connection pins for infra red receiver and target led 
#define LED 4
#define IR_RECEIVER 5

// connection pin for the speaker amplifier (relay)
#define SPEAKER_AMP 6

TM1637Display display(CLK, DIO);

uint8_t digit_0 = 0x3f;
uint8_t digit_1 = 0x06;
uint8_t digit_2 = 0x5b;
uint8_t digit_3 = 0x4f;
uint8_t digit_4 = 0x66;
uint8_t digit_5 = 0x6d;
uint8_t digit_6 = 0x7d;
uint8_t digit_7 = 0x07;
uint8_t digit_8 = 0x7f;
uint8_t digit_9 = 0x6f;

uint8_t points = 0x40;
uint8_t no_points = 0x80;

uint8_t clearDisplay[] = {0x00, 0x00, 0x00, 0x00};

String inputText = "";

bool alarmEnabled = false;

void setup()
{
  Serial.begin(9600);
  display.setBrightness(0xff);//max brightness
}

void loop()
{
  //read the serial input
  while (Serial.available() > 0) {
    char inChar = Serial.read();
    if (inChar == ';') {
      //delay(10000);
      //Serial.println("Message received: " + inputText);
      handleInput(inputText);
      inputText = "";
    }
    else {
      inputText += inChar;
    }
  }
  
  //check for hits in the infra red sensor if the alarm is enabled
  if (alarmEnabled) {
    if (digitalRead(IR_RECEIVER) == LOW) {//IR-receiver sends low when hit
      //inform the pi_clock that the alarm was "shot"
      Serial.println("alarm_hit");
      delay(100);//wait for 100 ms for the pi_clock to answer
    }
  }
  
  /*display.showNumberDecEx(100, points, true);
  delay(500);
  display.showNumberDecEx(100, no_points, true);
  delay(500);
  for (int i = 118; i >= 0; i--) {
    if (i%2 == 0) {
      display.showNumberDecEx(i/2, points, true);
    }
    else {
      display.showNumberDecEx(i/2, no_points, true);
    }
    delay(500);
  }

  for (int i = 0; i < 20; i++) {
    display.showNumberDecEx(0, points, true);
    delay(100);
    display.setSegments(clearDisplay);
    delay(100);
  }
  delay(5000);*/
}

void handleInput(String inputText) {
  char firstSign = inputText[0];
  switch (firstSign) {
    case 'C'://set clock
      setClockTime(inputText);
      break;
    case 'A'://set alarm enabled
      setAlarmEnabled(inputText);
      break;
    case 'T'://get temperature
      getTemperature();
      break;
    case 'H'://get humidity
      getHumidity();
      break;
    case 'S'://speaker amplifier
      setSpeakerAmplifierEnabled(inputText);
      break;
  }
}

void setClockTime(String inputText) {
  //input form: "C HHMM"
  
  String inputNumber = "";
  for (int i = 2; i < 6; i++) {
    inputNumber += inputText[i];
  }
  int number = inputNumber.toInt();
  
  display.showNumberDecEx(number, points, true);
}

void setAlarmEnabled(String inputText) {
  //input form: "A[0/1]" (where 1 is enabled and 0 is disabled)

  String inputStateText = "";
  inputStateText += inputText[1];
  int state = inputStateText.toInt();

  if (state == 1) {
    //enable the alarm switch and the LED
    alarmEnabled = true;
    digitalWrite(LED, HIGH);
    //TODO
  }
  else {
    //disable the alarm switch and the LED
    alarmEnabled = false;
    digitalWrite(LED, LOW);
    //TODO
  }
}

void setSpeakerAmplifierEnabled(String inputText) {
  //input form: "S[0/1]" (where 1 is enabled and 0 is disabled)
  
  String inputStateText = "";
  inputStateText += inputText[1];
  int state = inputStateText.toInt();
  
  if (state == 1) {
    //enable the speaker amplifier
    digitalWrite(SPEAKER_AMP, HIGH);
  }
  else {
    //disable the speaker amplifier
    digitalWrite(SPEAKER_AMP, LOW);
  }
}

void getTemperature() {
  //just send 42 till the method is implemented
  Serial.println("42");
  //TODO
}

void getHumidity() {
  //just send 42 till the method is implemented
  Serial.println("42");
  //TODO
}

