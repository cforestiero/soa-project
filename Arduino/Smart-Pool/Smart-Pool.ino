#define LOGS_ON

/*****************************************************
                      BIBLIOTECAS
******************************************************/

#include <OneWire.h>
#include <DallasTemperature.h>
#include <SoftwareSerial.h>
#include <Bounce2.h>

/*****************************************************
                        MACROS
******************************************************/

#define VERIFICATIONS_AMOUNT 6

#define SWITCH_FILTERING_MODE 1
#define SWITCH_DRAINING_MODE 0

#define TEMPERATURE_THRESHOLD 30
#define WATER_LEVEL_THRESHOLD 100
#define MEDIUM_LUMINOSITY_THRESHOLD 3
#define LOW_LUMINOSITY_THRESHOLD 1

#define MEDIUM_LIGHT_INTENSITY 0.5
#define LOW_LIGHT_INTENSITY 1

#define COMMUNICATION_BAUD_RATE 9600
#define INCHES_TO_CM 58.2
#define TRIG_TIME 1
#define LUMINOSITY_SCALING_FACTOR 10
#define LUMINOSITY_DARKNESS_RESISTANCE_KOHM 10
#define LUMINOSITY_LIGHT_RESISTANCE_KOHM 10
#define LUMINOSITY_CALIBRATION_RESISTANCE_KOHM 10
#define ANALOG_VOLT_MAX_REFERENCE 1024
#define MAX_VOLTAGE 5.0
#define VOLTAGE_TO_CELSIUS_OFFSET 0.5
#define VOLTAGE_TO_CELSIUS_FACTOR 100.0

#define RED_COLOUR_DEFAULT 255
#define GREEN_COLOUR_DEFAULT 255
#define BLUE_COLOUR_DEFAULT 0

#define COLOUR_MAX 255
#define COLOUR_MIN 0

#define INDEX_INITIALIZATION 0
#define INDEX_GET_TEMP 0

#define DEBOUNCE_INTERVAL 50

/*****************************************************
                        TIMERS
******************************************************/

#define TIMER_CAPTURE_EVENT 60
#define TIME_TO_STOP_WATER_PUMP 10000
#define TIME_TO_START_WATER_PUMP 10000
#define TIMER_FORCED_LIGHT_MODE 50000

/*****************************************************
                  PINES DE SENSORES
******************************************************/

#define PIN_LUMINOSITY_SENSOR A0

#define PIN_PROXIMITY_SENSOR_TRIG 6
#define PIN_PROXIMITY_SENSOR_ECHO 5
#define PIN_TEMPERATURE_SENSOR 7

#define PIN_WATER_PUMP_MODE_SWITCH_SENSOR 13

/*****************************************************
                  PINES DE ACTUADORES
******************************************************/

#define PIN_RELAY_ACTUATOR 2

#define PIN_RED_LED_ACTUATOR 11
#define PIN_GREEN_LED_ACTUATOR 10
#define PIN_BLUE_LED_ACTUATOR 9

/*****************************************************
                  PINES DE BLUETOOTH
******************************************************/

#define PIN_BLUETOOTH_RX 3
#define PIN_BLUETOOTH_TX 4
#define PIN_KEY 8

/*****************************************************
        VARIABLES DE ONEWIRE/DALLASTEMPERATURE
******************************************************/

OneWire oneWireBus(PIN_TEMPERATURE_SENSOR);
DallasTemperature sensor(&oneWireBus);

/*****************************************************
        VARIABLES DE BOUNCE2
******************************************************/

Bounce debouncer = Bounce();

/*****************************************************
                       ESTADOS
******************************************************/

enum possibleStates 
{
  IDLE,
  DRAINING_MODE,
  DRAINING_DAY_MODE,
  DRAINING_PROCESS_DAY,
  DRAINING_NIGHT_MODE,
  DRAINING_PROCESS_NIGHT,
  FILTERING_MODE,
  FILTERING_DAY_MODE,
  FILTERING_PROCESS_DAY,
  FILTERING_NIGHT_MODE,
  FILTERING_PROCESS_NIGHT
};

/*****************************************************
                       EVENTOS
******************************************************/

enum possibleEvents 
{
  SWITCH_DRAINING,
  SWITCH_FILTERING,
  HIGH_LIGHT,
  MEDIUM_LIGHT,
  LOW_LIGHT,
  TIMER_START_WATER_PUMP,
  TIMER_STOP_WATER_PUMP,
  MAX_TEMPERATURE,
  LOW_WATER_LEVEL,
  BLUETOOTH_SIGNAL_READY,
  BLUETOOTH_SIGNAL_LIGHT_COLOUR,
  BLUETOOTH_SIGNAL_LIGHT_MODE,
  BLUETOOTH_SIGNAL_SEND_INFORMATION,
  EVENT_CONTINUE
};

/*****************************************************
                  VARIABLES GLOBALES
******************************************************/

possibleStates currentState;
possibleEvents eventType;
unsigned long previousTime;
unsigned long previousWaterPumpTime;
unsigned long previousForcedLightTime;
unsigned long currentTime;
unsigned long timeToStartWaterPump = TIME_TO_START_WATER_PUMP;
bool isWaterPumpON;

// Espera luego de modificar manualmente las luces LED, para que se mantenga a pesar de los eventos generados por el sensor de luminosidad
bool lightManuallyChangedRecently;

// Indice para vector de funciones
int index = INDEX_INITIALIZATION;

// Información de Sensores
bool modePressed;
float waterTemperatureCelsius;
float waterDistanceCM;
float placeLuminosity;

// RGB
int redColour;
int greenColour;
int blueColour;

// Comando para BTString
char commandBTApp;


/*****************************************************
              CREACION DE SOFTWARESERIAL
******************************************************/

SoftwareSerial BTSerial(PIN_BLUETOOTH_RX, PIN_BLUETOOTH_TX);
String nombreBT = "Smart-Pool";
int bpsBT = COMMUNICATION_BAUD_RATE;
String passBT = "SmartPool";
String comandoName = "AT+NAME=" + nombreBT;
String comandoPass = "AT+PSWD=" + passBT;

/*****************************************************
              FUNCIONES DE BLUETOOTH
******************************************************/

void sendCurrentInformation(char command)
{
  String currentMode = (modePressed == SWITCH_FILTERING_MODE) ? "F" : "D";
  String currentStateForBT = getStateName(currentState);

  switch (commandBTApp) 
  {
    case 'A':
      Serial.println("Config filtrado");
      break;
    case 'B':
      // Evento BT para enviar el estado de la bomba
      BTSerial.println("B," + currentMode);
      break;
    case 'C':
      Serial.println("Cambiaria Color");
      break;
    case 'D':
      Serial.println("Me pondre a desagotar");
      break;
    case 'I':
      BTSerial.println("I, Temperatura del Agua: " + String(waterTemperatureCelsius) + " ºC" + "," + String(waterDistanceCM)) ;
      break;
    case 'L':
      // Evento BT para enviar el estado de la luz
      BTSerial.println("L," + currentStateForBT);
      break;
    case 'W':
      Serial.println("Cambio de modo dia/noche");
      break;
    default: Serial.println("Unknown command");
      break;
  }

}


/*****************************************************
                   FUNCIONES DE LOGS
******************************************************/

/*
   Función: generateLog
   Descripción: Esta función genera los logs para informar los cambios de estados, mostrándolos por la terminal

   Parámetros:
   - initialState: Estado inicial del embebido
   - currentEvent: Evento capturado
   - finalState: Estado al que transiciona el embebido
*/

void generateLog(const char *initialState, const char *currentEvent, const char *finalState)
{
#ifdef LOGS_ON
  Serial.println("................................................");
  Serial.println("Estado Inicial: " + String(initialState));
  Serial.println("Evento: " + String(currentEvent));
  Serial.println("Estado Final: " + String(finalState));
  Serial.println("................................................");
#endif
  generateLogBT(finalState, currentEvent);
}

void generateLogBT(const char *finalState, const char *currentEvent)
{
    BTSerial.println("E," + String(finalState) + "," + String(currentEvent));
}

/*****************************************************
                   FUNCIONES DE STATES
******************************************************/

String getStateName(possibleStates state)
{
    switch (state) 
    {
        case IDLE:
            return "IDL";
        case DRAINING_MODE:
            return "DM";
        case DRAINING_DAY_MODE:
            return "DDM";
        case DRAINING_PROCESS_DAY:
            return "DPD";
        case DRAINING_NIGHT_MODE:
            return "DNM";
        case DRAINING_PROCESS_NIGHT:
            return "DPN";
        case FILTERING_MODE:
            return "FM";
        case FILTERING_DAY_MODE:
            return "FDM";
        case FILTERING_PROCESS_DAY:
            return "FPD";
        case FILTERING_NIGHT_MODE:
            return "FNM";
        case FILTERING_PROCESS_NIGHT:
            return "FPN";
        default:
            return "UK";
    }
}

/*****************************************************
                   FUNCIONES DE SENSORES
******************************************************/

/*
   Función: readTemperature
   Descripción: Esta función lee la temperatura del sensor, convirtiendo la lectura en voltaje y al final en grados Celsius

   Retorna:
   - temperatureCelsius: La temperatura leída por el sensor, expresada en grados Celsius
*/

float readTemperature()
{
  float temperatureCelsius;
  sensor.requestTemperatures();

  return temperatureCelsius = sensor.getTempCByIndex(INDEX_GET_TEMP);
}

/*
   Función: readLuminosity
   Descripción: Esta función lee la luminosidad del sensor, aplica la función Valor lumínico [lux] = (( 1024 - V ) x A x 10) / ( B x Rc x V ) para conocer el valor de la misma y retornarlo

   Retorna:
   - luminosity: La luminosidad leída por el sensor
*/

float readLuminosity()
{
  int lightVoltage = analogRead(PIN_LUMINOSITY_SENSOR);
  float luminosity = ((ANALOG_VOLT_MAX_REFERENCE - lightVoltage) * LUMINOSITY_DARKNESS_RESISTANCE_KOHM * LUMINOSITY_SCALING_FACTOR) / (LUMINOSITY_LIGHT_RESISTANCE_KOHM * LUMINOSITY_CALIBRATION_RESISTANCE_KOHM * lightVoltage);

  return luminosity;
}

/*
   Función: measureDistance
   Descripción: Esta función calcula la distancia. Primero genera un pulso corto en el trigger y calcula cuánto tarda en llegar el eco

   Retorna:
   - distanceCM: La distancia medida a partir del sensor, expresada en centímetros
*/

float measureDistance()
{

  // Generar un pulso corto en el pin de trigger
  digitalWrite(PIN_PROXIMITY_SENSOR_TRIG, HIGH);
  delayMicroseconds(TRIG_TIME);
  digitalWrite(PIN_PROXIMITY_SENSOR_TRIG, LOW);

  // Medir el tiempo que tarda en llegar el eco
  float duration = pulseIn(PIN_PROXIMITY_SENSOR_ECHO, HIGH);
  float distanceCM = duration / INCHES_TO_CM;

  return distanceCM;
}

/*****************************************************
                FUNCIONES DE ACTUADORES
******************************************************/

/*
   Función: turnOFFLED
   Descripción: Esta función apaga las luces LED
*/

void turnOFFLED()
{
  analogWrite(PIN_RED_LED_ACTUATOR, COLOUR_MIN);
  analogWrite(PIN_GREEN_LED_ACTUATOR, COLOUR_MIN);
  analogWrite(PIN_BLUE_LED_ACTUATOR, COLOUR_MIN);
}

/*
   Función: turnONLED
   Descripción: Esta función prende las luces LED a cierta intensidad, en color blanco

   Parámetros:
   - intensity: Intensidad a la que se desea prender las luces LED (en el embebido actualmente se encuentra intensidad media o alta)
*/

void turnONLED(float intensity) 
{
  analogWrite(PIN_RED_LED_ACTUATOR, COLOUR_MAX * intensity);
  analogWrite(PIN_GREEN_LED_ACTUATOR, COLOUR_MAX * intensity);
  analogWrite(PIN_BLUE_LED_ACTUATOR, COLOUR_MAX * intensity);
}

/*
   Función: modifyLEDColour
   Descripción: Esta función prende las luces LED en cierto color, expresado en valores RGB

   Parámetros:
   - red: Componente rojo del color
   - green: Componente verde del color
   - blue: Componente azul del color
*/

void modifyLEDColour(int red, int green, int blue)
{
  analogWrite(PIN_RED_LED_ACTUATOR, red);
  analogWrite(PIN_GREEN_LED_ACTUATOR, green);
  analogWrite(PIN_BLUE_LED_ACTUATOR, blue);
}

/*
   Función: turnOFFWaterPump
   Descripción: Esta función apaga el relé al que se conecta la bomba de agua
*/

void turnOFFWaterPump()
{
  digitalWrite(PIN_RELAY_ACTUATOR, LOW);
  isWaterPumpON = false;
}

/*
   Función: turnONWaterPump
   Descripción: Esta función prende el relé al que se conecta la bomba de agua
*/

void turnONWaterPump()
{
  digitalWrite(PIN_RELAY_ACTUATOR, HIGH);
  isWaterPumpON = true;
}

/*****************************************************
                  CAPTURA DE EVENTOS
******************************************************/

/*
   Función: verifyModeSwitch
   Descripción: Esta función lee el switch que permite modificar el modo Filtrado/Drenaje de la bomba

   Eventos Posibles:
   - SWITCH_FILTERING_MODE: En caso de encontrarse en la primera posición
   - SWITCH_DRAINING_MODE: En caso de encontrarse en la segunda posición
*/

void verifyModeSwitch()
{
  debouncer.update();
  modePressed = digitalRead(PIN_WATER_PUMP_MODE_SWITCH_SENSOR);

  if (modePressed == SWITCH_FILTERING_MODE)
    eventType = SWITCH_FILTERING;
  else if (modePressed == SWITCH_DRAINING_MODE)
    eventType = SWITCH_DRAINING;
  else
    eventType = EVENT_CONTINUE;
}

/*
   Función: verifyTemperature
   Descripción: Esta función verifica el valor de la temperatura del agua

   Eventos Posibles:
   - MAX_TEMPERATURE: En caso de que se supere la temperatura establecida como umbral
   - EVENT_CONTINUE: En caso de que no se supere la temperatura establecida como umbral
*/

void verifyTemperature()
{
  waterTemperatureCelsius = readTemperature();

  if (waterTemperatureCelsius > TEMPERATURE_THRESHOLD)
    eventType = MAX_TEMPERATURE;
  else
    eventType = EVENT_CONTINUE;
}

/*
   Función: verifyWaterLevel
   Descripción: Esta función verifica el nivel del agua

   Eventos Posibles:
   - LOW_WATER_LEVEL: En caso de que el nivel del agua sea menor a la establecida como umbral
   - EVENT_CONTINUE: En caso de que el nivel del agua sea mayor a la establecida como umbral
*/

void verifyWaterLevel()
{
  waterDistanceCM = measureDistance();

  if (waterDistanceCM >= WATER_LEVEL_THRESHOLD)
    eventType = LOW_WATER_LEVEL;
  else
    eventType = EVENT_CONTINUE;
}

/*
   Función: verifyLight
   Descripción: Esta función verifica la luminosidad del ambiente

   Eventos Posibles:
   - HIGH_LIGHT: En caso de que la luminosidad sea mayor a la establecida como umbral (menor a los valores del sensor de luminosidad establecidos como umbral)
   - MEDIUM_LIGHT: En caso de que la luminosidad se encuentre a la establecida como umbral medio (entre los valores del sensor de luminosidad considerados como medio y máximo)
   - LOW_LIGHT: En caso de que la luminosidad sea menor a la establecida como umbral (mayor a los valores del sensor de luminosidad establecidos como umbral)
*/

void verifyLight()
{
  placeLuminosity = readLuminosity();
  if (lightManuallyChangedRecently && (currentTime - previousForcedLightTime) <= TIMER_FORCED_LIGHT_MODE)
    eventType = EVENT_CONTINUE;
  else if (placeLuminosity <= LOW_LUMINOSITY_THRESHOLD) 
  {
    lightManuallyChangedRecently = false;
    eventType = LOW_LIGHT;
  } else if (placeLuminosity <= MEDIUM_LUMINOSITY_THRESHOLD) 
  {
    lightManuallyChangedRecently = false;
    eventType = MEDIUM_LIGHT;
  } else 
  {
    lightManuallyChangedRecently = false;
    eventType = HIGH_LIGHT;
  }
}

/*
   Función: verifyTimersWaterPump
   Descripción: Esta función verifica si se cumplió el plazo para que la bomba inicie o se detenga cuando está en modo filtrado

   Eventos Posibles:
   - TIMER_START_WATER_PUMP: En caso de que la bomba se encuentre apagada, se encuentre en modo filtrado y se alcance el tiempo de intervalo para comenzar el filtrado
   - TIMER_STOP_WATER_PUMP: En caso de que la bomba se encuentre prendida, se encuentre en modo filtrado y se alcance el tiempo de intervalo para finalizar el filtrado
   - EVENT_CONTINUE: En caso de que no se cumpla ninguna de las condiciones arriba especificadas
*/

void verifyTimersWaterPump()
{
  if ((currentTime - previousWaterPumpTime) > timeToStartWaterPump && !isWaterPumpON && modePressed == SWITCH_FILTERING_MODE)
    eventType = TIMER_START_WATER_PUMP;
  else if ((currentTime - previousWaterPumpTime) > timeToStartWaterPump && isWaterPumpON && modePressed == SWITCH_FILTERING_MODE)
    eventType = TIMER_STOP_WATER_PUMP;
  else
    eventType = EVENT_CONTINUE;
}

/*
   Función: verifyBTCommand
   Descripción: Esta función verifica si se recibe una señal BlueTooth en la cual se desee ejecutar un comando

   Eventos Posibles:
   - BLUETOOTH_SIGNAL_READY: En caso de que se reciba la señal READY_FOR_DRAINING(D)
   - BLUETOOTH_SIGNAL_LIGHT_COLOUR: En caso de que se reciba la señal CHANGE_LIGHT_COLOUR
   - BLUETOOTH_SIGNAL_LIGHT_MODE: En caso de que se reciba la señal CHANGE_LIGHT_MODE
   - EVENT_CONTINUE: En caso de que no se reciba ninguna señal (no se envíe ningún comando)
*/

void verifyBTCommand()
{

  String bufferBT;

  if (BTSerial.available())
  {
    char receivedChar = BTSerial.read();
    commandBTApp = receivedChar;
    Serial.print("Received: ");
    Serial.println(receivedChar);
    switch (receivedChar)
    {
      case 'A':
        Serial.println("Command A received");
        bufferBT = BTSerial.read();
        Serial.println(bufferBT);
        timeToStartWaterPump = BTSerial.parseInt();
        Serial.println("Tiempo recibido");
        Serial.println(timeToStartWaterPump);
        break;
      case 'B':
        // Evento BT para enviar el estado de la bomba
        sendCurrentInformation(commandBTApp);
        break;
      case 'C':
        eventType = BLUETOOTH_SIGNAL_LIGHT_COLOUR;
        Serial.println("Cambia Color por la app");
        bufferBT = BTSerial.read();

        redColour = BTSerial.parseInt();
        greenColour = BTSerial.parseInt();
        blueColour = BTSerial.parseInt();

        sendCurrentInformation(commandBTApp);
        break;
      case 'D':
        Serial.println("Command D para drenar");
        eventType = BLUETOOTH_SIGNAL_READY;
        break;
      case 'I':
        eventType = BLUETOOTH_SIGNAL_SEND_INFORMATION;
        break;
      case 'L':
        // Evento BT para enviar el estado de las luces
        sendCurrentInformation(commandBTApp);
        break;
      case 'W':
        // Evento BT para modificar el estado de la luz
        eventType = BLUETOOTH_SIGNAL_LIGHT_MODE;
        Serial.println("Cambia de modo para apagar o prender luz");
        break;
      default:
        Serial.println("Unknown command");
        break;
    }
  }
}


void (*verifySensor[VERIFICATIONS_AMOUNT])() = { verifyModeSwitch, verifyWaterLevel, verifyTemperature, verifyLight, verifyTimersWaterPump, verifyBTCommand };

/*
   Función: captureEvent
   Descripción: Esta función se encarga de constantemente capturar los distintos eventos generados por los sensores y timers del embebido
*/

void captureEvent()
{
  currentTime = millis();

  if ((currentTime - previousTime) > TIMER_CAPTURE_EVENT)
  {
    verifySensor[index]();
    index = ++index % VERIFICATIONS_AMOUNT;
    previousTime = currentTime;
  } else
    eventType = EVENT_CONTINUE;
}


/*****************************************************
                    INICIALIZACIÓN
******************************************************/

/*
   Función: setUpEmbeddedSystem
   Descripción: Esta función se encarga de inicializar diversos aspectos del embebido (comunicación serial, pines, estado inicial y temporizador)
   Solamente se ejecutará al inicio del programa Arduino
*/

void setUpEmbeddedSystem()
{
  // Inicialización de comunicación serial
  Serial.begin(COMMUNICATION_BAUD_RATE);

  // Inicialización de comunicación con Bluetooth
  BTSerial.begin(bpsBT);

  // Inicialización de la variable sensor para la temperatura
  sensor.begin();

  // Inicialización de pines en modo INPUT
  pinMode(PIN_TEMPERATURE_SENSOR, INPUT);
  pinMode(PIN_LUMINOSITY_SENSOR, INPUT);
  pinMode(PIN_PROXIMITY_SENSOR_ECHO, INPUT);

  // Inicialización de pines en modo INPUT_PULLUP
  pinMode(PIN_WATER_PUMP_MODE_SWITCH_SENSOR, INPUT_PULLUP);

  // Inicialización de pines en modo OUTPUT
  pinMode(PIN_PROXIMITY_SENSOR_TRIG, OUTPUT);
  pinMode(PIN_RELAY_ACTUATOR, OUTPUT);
  pinMode(PIN_RED_LED_ACTUATOR, OUTPUT);
  pinMode(PIN_GREEN_LED_ACTUATOR, OUTPUT);
  pinMode(PIN_BLUE_LED_ACTUATOR, OUTPUT);

  // Inicialización Bounce2 para interruptor
  debouncer.attach(PIN_WATER_PUMP_MODE_SWITCH_SENSOR); // Asocia el pin al debouncer
  debouncer.interval(DEBOUNCE_INTERVAL); // Establece el intervalo de debounce en milisegundos

  // Inicialización para AT
  pinMode(PIN_KEY, OUTPUT);
  digitalWrite(PIN_KEY, HIGH);

  // Comando AT con el nombre y contraseña
  BTSerial.println(comandoName);
  BTSerial.println(comandoPass);

  // Inicialización del Estado de la FSM
  currentState = IDLE;

  // Verificar estado switch
  verifyModeSwitch();

  Serial.println("Inicializando");

  // Inicialización de Temporizador
  previousTime = millis();
}

void fsm()
{
  captureEvent();

  switch (currentState)
  {
    case IDLE:
      switch (eventType)
      {
        case SWITCH_DRAINING:
          generateLog("IDLE", "SWITCH_DRAINING", "DRAINING_MODE");
          currentState = DRAINING_MODE;
          break;
        case SWITCH_FILTERING:
          generateLog("IDLE", "SWITCH_DRAINING", "FILTERING_MODE");
          currentState = FILTERING_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = IDLE;
          break;
        case EVENT_CONTINUE:
          currentState = IDLE;
          break;
      }
      break;
    case DRAINING_MODE:
      switch (eventType)
      {
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("DRAINING_MODE", "HIGH_LIGHT", "DRAINING_DAY_MODE");
          currentState = DRAINING_DAY_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("DRAINING_MODE", "MEDIUM_LIGHT", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("DRAINING_MODE", "LOW_LIGHT", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case SWITCH_FILTERING:
          generateLog("DRAINING_MODE", "SWITCH_FILTERING", "FILTERING_MODE");
          currentState = FILTERING_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = DRAINING_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = DRAINING_MODE;
          break;
      }
      break;
    case DRAINING_DAY_MODE:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnONLED(LOW_LIGHT_INTENSITY);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_DAY_MODE", "BLUETOOTH_SIGNAL_LIGHT_MODE", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("DRAINING_DAY_MODE", "MEDIUM_LIGHT", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("DRAINING_DAY_MODE", "LOW_LIGHT", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case SWITCH_FILTERING:
          generateLog("DRAINING_DAY_MODE", "SWITCH_FILTERING", "FILTERING_DAY_MODE");
          currentState = FILTERING_DAY_MODE;
          break;
        case BLUETOOTH_SIGNAL_READY:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("DRAINING_DAY_MODE", "BLUETOOTH_SIGNAL_READY", "DRAINING_PROCESS_DAY");
          currentState = DRAINING_PROCESS_DAY;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = DRAINING_DAY_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = DRAINING_DAY_MODE;
          break;
      }
      break;
    case DRAINING_PROCESS_DAY:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnONLED(LOW_LIGHT_INTENSITY);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_PROCESS_DAY", "BLUETOOTH_SIGNAL_LIGHT_MODE", "DRAINING_PROCESS_NIGHT");
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("DRAINING_PROCESS_DAY", "MEDIUM_LIGHT", "DRAINING_PROCESS_NIGHT");
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("DRAINING_PROCESS_DAY", "LOW_LIGHT", "DRAINING_PROCESS_NIGHT");
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case LOW_WATER_LEVEL:
          turnOFFWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("DRAINING_PROCESS_DAY", "LOW_WATER_LEVEL", "IDLE");
          currentState = IDLE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = DRAINING_PROCESS_DAY;
          break;
        case EVENT_CONTINUE:
          currentState = DRAINING_PROCESS_DAY;
          break;
      }
      break;
    case DRAINING_NIGHT_MODE:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnOFFLED();
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_NIGHT_MODE", "BLUETOOTH_SIGNAL_LIGHT_MODE", "DRAINING_DAY_MODE");
          currentState = DRAINING_DAY_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          currentState = DRAINING_NIGHT_MODE;
          break;
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("DRAINING_NIGHT_MODE", "HIGH_LIGHT", "DRAINING_DAY_MODE");
          currentState = DRAINING_DAY_MODE;
          break;
        case SWITCH_FILTERING:
          generateLog("DRAINING_NIGHT_MODE", "SWITCH_FILTERING", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case BLUETOOTH_SIGNAL_READY:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("DRAINING_NIGHT_MODE", "BLUETOOTH_SIGNAL_READY", "DRAINING_PROCESS_NIGHT");
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_COLOUR:
          modifyLEDColour(redColour, greenColour, blueColour);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_NIGHT_MODE", "BLUETOOTH_SIGNAL_LIGHT_COLOUR", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = DRAINING_NIGHT_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = DRAINING_NIGHT_MODE;
          break;
      }
      break;
    case DRAINING_PROCESS_NIGHT:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnOFFLED();
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_PROCESS_NIGHT", "BLUETOOTH_SIGNAL_LIGHT_MODE", "DRAINING_PROCESS_DAY");
          currentState = DRAINING_PROCESS_DAY;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("DRAINING_PROCESS_NIGHT", "HIGH_LIGHT", "DRAINING_PROCESS_DAY");
          currentState = DRAINING_PROCESS_DAY;
          break;
        case LOW_WATER_LEVEL:
          turnOFFWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("DRAINING_PROCESS_NIGHT", "LOW_WATER_LEVEL", "IDLE");
          currentState = IDLE;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_COLOUR:
          modifyLEDColour(redColour, greenColour, blueColour);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("DRAINING_PROCESS_NIGHT", "BLUETOOTH_SIGNAL_LIGHT_COLOUR", "DRAINING_PROCESS_NIGHT");
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = DRAINING_PROCESS_NIGHT;
          break;
        case EVENT_CONTINUE:
          currentState = DRAINING_PROCESS_NIGHT;
          break;
      }
      break;
    case FILTERING_MODE:
      switch (eventType)
      {
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("FILTERING_MODE", "HIGH_LIGHT", "FILTERING_DAY_MODE");
          currentState = FILTERING_DAY_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("FILTERING_MODE", "MEDIUM_LIGHT", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("FILTERING_MODE", "LOW_LIGHT", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case SWITCH_DRAINING:
          generateLog("FILTERING_MODE", "SWITCH_DRAINING", "DRAINING_MODE");
          currentState = DRAINING_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = FILTERING_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = FILTERING_MODE;
          break;
      }
      break;
    case FILTERING_DAY_MODE:
      switch (eventType)
      {
        case MAX_TEMPERATURE:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_DAY_MODE", "MAX_TEMPERATURE", "FILTERING_PROCESS_DAY");
          currentState = FILTERING_PROCESS_DAY;
          break;
        case TIMER_START_WATER_PUMP:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_DAY_MODE", "TIMER_START_WATER_PUMP", "FILTERING_PROCESS_DAY");
          currentState = FILTERING_PROCESS_DAY;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnONLED(LOW_LIGHT_INTENSITY);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_DAY_MODE", "BLUETOOTH_SIGNAL_LIGHT_MODE", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("FILTERING_DAY_MODE", "MEDIUM_LIGHT", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("FILTERING_DAY_MODE", "LOW_LIGHT", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case SWITCH_DRAINING:
          generateLog("FILTERING_DAY_MODE", "SWITCH_DRAINING", "DRAINING_DAY_MODE");
          currentState = DRAINING_DAY_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = FILTERING_DAY_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = FILTERING_DAY_MODE;
          break;
      }
      break;
    case FILTERING_PROCESS_DAY:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnONLED(LOW_LIGHT_INTENSITY);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_PROCESS_DAY", "BLUETOOTH_SIGNAL_LIGHT_MODE", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          generateLog("FILTERING_PROCESS_DAY", "MEDIUM_LIGHT", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case LOW_LIGHT:
          turnONLED(LOW_LIGHT_INTENSITY);
          generateLog("FILTERING_PROCESS_DAY", "LOW_LIGHT", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case TIMER_STOP_WATER_PUMP:
          turnOFFWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_PROCESS_DAY", "TIMER_STOP_WATER_PUMP", "IDLE");
          currentState = IDLE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = FILTERING_PROCESS_DAY;
          break;
        case EVENT_CONTINUE:
          currentState = FILTERING_PROCESS_DAY;
          break;
      }
      break;
    case FILTERING_NIGHT_MODE:
      switch (eventType)
      {
        case MAX_TEMPERATURE:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_NIGHT_MODE", "MAX_TEMPERATURE", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case TIMER_START_WATER_PUMP:
          turnONWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_NIGHT_MODE", "TIMER_START_WATER_PUMP", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnOFFLED();
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_NIGHT_MODE", "BLUETOOTH_SIGNAL_LIGHT_MODE", "FILTERING_DAY_MODE");
          currentState = FILTERING_DAY_MODE;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          currentState = FILTERING_NIGHT_MODE;
          break;
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("FILTERING_NIGHT_MODE", "HIGH_LIGHT", "FILTERING_DAY_MODE");
          currentState = FILTERING_DAY_MODE;
          break;
        case SWITCH_DRAINING:
          generateLog("FILTERING_NIGHT_MODE", "SWITCH_DRAINING", "DRAINING_NIGHT_MODE");
          currentState = DRAINING_NIGHT_MODE;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_COLOUR:
          modifyLEDColour(redColour, greenColour, blueColour);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_NIGHT_MODE", "BLUETOOTH_SIGNAL_LIGHT_COLOUR", "FILTERING_NIGHT_MODE");
          currentState = FILTERING_NIGHT_MODE;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = FILTERING_NIGHT_MODE;
          break;
        case EVENT_CONTINUE:
          currentState = FILTERING_NIGHT_MODE;
          break;
      }
      break;
    case FILTERING_PROCESS_NIGHT:
      switch (eventType)
      {
        case BLUETOOTH_SIGNAL_LIGHT_MODE:
          turnOFFLED();
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_PROCESS_NIGHT", "BLUETOOTH_SIGNAL_LIGHT_MODE", "FILTERING_PROCESS_DAY");
          currentState = FILTERING_PROCESS_DAY;
          break;
        case MEDIUM_LIGHT:
          turnONLED(MEDIUM_LIGHT_INTENSITY);
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case HIGH_LIGHT:
          turnOFFLED();
          generateLog("FILTERING_PROCESS_NIGHT", "HIGH_LIGHT", "FILTERING_PROCESS_DAY");
          currentState = FILTERING_PROCESS_DAY;
          break;
        case TIMER_STOP_WATER_PUMP:
          turnOFFWaterPump();
          previousWaterPumpTime = currentTime;
          generateLog("FILTERING_PROCESS_NIGHT", "TIMER_STOP_WATER_PUMP", "IDLE");
          currentState = IDLE;
          break;
        case BLUETOOTH_SIGNAL_LIGHT_COLOUR:
          modifyLEDColour(redColour, greenColour, blueColour);
          lightManuallyChangedRecently = true;
          previousForcedLightTime = currentTime;
          generateLog("FILTERING_PROCESS_NIGHT", "BLUETOOTH_SIGNAL_LIGHT_COLOUR", "FILTERING_PROCESS_NIGHT");
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case BLUETOOTH_SIGNAL_SEND_INFORMATION:
          sendCurrentInformation(commandBTApp);
          currentState = FILTERING_PROCESS_NIGHT;
          break;
        case EVENT_CONTINUE:
          currentState = FILTERING_PROCESS_NIGHT;
          break;
      }
      break;
  }
}


void setup()
{
  setUpEmbeddedSystem();
}

void loop()
{
  fsm();
}
