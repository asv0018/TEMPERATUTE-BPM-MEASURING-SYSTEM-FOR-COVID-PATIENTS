#include <DallasTemperature.h>
#include <FirebaseESP8266.h>
#include <ESP8266WiFi.h>
#include <OneWire.h>
#include <WiFiUdp.h>
#include <NTPClient.h>

#define WIFI_SSID "MAHATHI TEAM"
#define WIFI_PASSWORD "Mahathi123"

#define FIREBASE_HOST "temp-pulse-metering-app-default-rtdb.firebaseio.com"

#define FIREBASE_AUTH "lgUQVTE68zRoeGsYeGoHosznIObeKpucf4ipIecO"

#define ONE_WIRE_BUS 4

OneWire oneWire(ONE_WIRE_BUS);
 
DallasTemperature sensors(&oneWire);

FirebaseData fbdo;

FirebaseJson json;

int getResponse(FirebaseData &data);

int inbuilt_led = 2;

void printError(FirebaseData &data);

// Define NTP Client to get time
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

void setup(void){
    pinMode(inbuilt_led,OUTPUT);
    Serial.begin(115200); 
    sensors.begin();
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to Wi-Fi");
    while (WiFi.status() != WL_CONNECTED){
      Serial.print(".");
      delay(300);
    }
    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();
    
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
    Firebase.reconnectWiFi(true);

    timeClient.begin();
    // Set offset time in seconds to adjust for your timezone, for example:
    // GMT +1 = 3600
    // GMT +8 = 28800
    // GMT -1 = -3600
    // GMT 0 = 0
    timeClient.setTimeOffset(((5*(3600))+1800));

    //Set the size of WiFi rx/tx buffers in the case where we want to work with large data.
    fbdo.setBSSLBufferSize(1024, 1024);
  
    //Set the size of HTTP response buffers in the case where we want to work with large data.
    fbdo.setResponseSize(1024);
  
    //Set database read timeout to 1 minute (max 15 minutes)
    Firebase.setReadTimeout(fbdo, 1000 * 60);
    //tiny, small, medium, large and unlimited.
    //Size and its write timeout e.g. tiny (1s), small (10s), medium (30s) and large (60s).
    Firebase.setwriteSizeLimit(fbdo, "tiny");
}

float temperature, bpm;

void loop(void){
  digitalWrite(inbuilt_led, HIGH);
    if(Firebase.getBool(fbdo, "PARAMETERS/is_data_requested")){
      bool temp_state = fbdo.boolData();
      if(temp_state==true){
        digitalWrite(inbuilt_led, LOW);
        Serial.println("DATA REQUESTED FROM THE APP");
        sensors.requestTemperatures();
        temperature = sensors.getTempFByIndex(0);
        bpm = analogRead(A0);
        createPayload();
        timeClient.update();
        String currentTime = timeClient.getFormattedTime();
        unsigned long epochTime = timeClient.getEpochTime();
        struct tm *ptm = gmtime ((time_t *)&epochTime); 
        int monthDay = ptm->tm_mday;
        int currentMonth = ptm->tm_mon+1;
        int currentYear = ptm->tm_year+1900;
        String currentDate = String(monthDay)+"-"+String(currentMonth)+"-"+String(currentYear);
        delay(3000);
        Serial.print("Current date: ");
        Serial.println(currentDate);
        Serial.print("Current time: ");
        Serial.println(currentTime);
        json.clear();
        json.add("REQUESTED_TIME", currentTime);
        json.add("REQUESTED_DATE", currentDate);
        json.add("TEMPERATURE", temperature);
        json.add("HEART_BPM", bpm);
        Firebase.updateNode(fbdo, "/REQUESTED_DATA", json);
        json.clear();
        json.add("HEART_BPM", bpm);
        json.add("TEMPERATURE", temperature);
        Firebase.updateNode(fbdo, "HISTORY/"+currentDate+"/"+currentTime, json);
        Firebase.setBool(fbdo, "PARAMETERS/is_data_requested", false);
        
      }
      
    }
    
}


void printError(FirebaseData &data){
  Serial.println("------------------------------------");
  Serial.println("FAILED");
  Serial.println("REASON: " + fbdo.errorReason());
  Serial.println("------------------------------------");
}

int getResponse(FirebaseData &data){
  if (data.dataType() == "int")
    return data.intData();
  else
    return 100;
}

void createPayload(){
  temperature = random(93,96);
  bpm = random(80,89);
}
