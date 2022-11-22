#include <SoftwareSerial.h>

SoftwareSerial BTSerial(7, 6);

const int pin = 4;

String cmd="";

String key_high = "31";
String key_low = "69";

void setup() {
  // put your setup code here, to run once:
  pinMode(pin, OUTPUT);

  Serial.begin(9600);
  BTSerial.begin(9600);
}


String str = "";
char temp;
void loop() {
  // put your main code here, to run repeatedly:

  while(BTSerial.available()) {
    temp = BTSerial.read();
    str = str + temp;
    Serial.println(str);
  }

  if(str == key_high){
     digitalWrite(pin, HIGH);
  }
   
   if(str == key_low) {
     digitalWrite(pin, LOW);
   }

   str = "";
}
