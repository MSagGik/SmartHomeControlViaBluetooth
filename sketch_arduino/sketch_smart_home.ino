#include "SPI.h"
#include <GyverBME280.h> // библиотека сенсоров
#include <GyverOLED.h> // библиотека OLED  экрана

// пины светодиодов
#define LED_PIN_1 1
#define LED_PIN_2 2
#define LED_PIN_3 3
#define LED_PIN_4 4
#define LED_PIN_5 5
#define LED_PIN_6 6
#define LED_PIN_7 7
#define LED_PIN_8 8
#define LED_PIN_9 9
#define LED_PIN_10 10
#define LED_PIN_11 11
#define LED_PIN_12 12

GyverBME280 sensor; // сенсор
GyverOLED<SSD1306_128x64, OLED_NO_BUFFER> oled_monitor; // монитор

// метод выполняемый на старте и финише работы контроллера
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600); //  запуск порта
  sensor.begin(); // инициализация сенсора (температура и давление)
  // задание параметров экрана
  oled_monitor.init();        // инициализация
  oled_monitor.clear();       // очистка
  oled_monitor.setScale(1);   // масштаб текста (1..4)
  oled_monitor.home();        // курсор в 0,0
  oled_monitor.print("Метеостанция 1.0");
  oled_monitor.setCursor(0, 3); // перевод курсора на 3 строку
  oled_monitor.println("Данные температуры");
  oled_monitor.println("и давления");
  // инициализация светодиодов
  pinMode(LED_PIN_2, OUTPUT);
  pinMode(LED_PIN_3, OUTPUT);
  pinMode(LED_PIN_4, OUTPUT);
  pinMode(LED_PIN_5, OUTPUT);
  pinMode(LED_PIN_6, OUTPUT);
  pinMode(LED_PIN_7, OUTPUT);
  pinMode(LED_PIN_8, OUTPUT);
  pinMode(LED_PIN_9, OUTPUT);
  pinMode(LED_PIN_10, OUTPUT);
  pinMode(LED_PIN_11, OUTPUT);
  pinMode(LED_PIN_12, OUTPUT);
  delay(1000);
}

// цикличный метод запускаемый после метода setup()
void loop() {

  if(!Serial.available()) { // если по порту нет данных
  
  // считывание данных сенсора
  float pressurePascal = sensor.readPressure(); // считывание давления в Паскалях
  float pressureClassic = pressureToMmHg(pressurePascal);  // конвертирование давления в мм.рт.ст.
  float temperature = sensor.readTemperature(); // считывание температуры в градусах
  
  // вывод климатических данных на монитор
  oled_monitor.clear();       // очистка
  oled_monitor.setScale(1);   // масштаб текста (1..4)
  oled_monitor.home();        // курсор в 0,0
  oled_monitor.print("Метеостанция 1.0");
  oled_monitor.setCursor(0, 3); // перевод курсора на 3 строку
  oled_monitor.print("Температура ");
  oled_monitor.print(temperature);
  oled_monitor.println(" *С");
  oled_monitor.println("Давление:");
  oled_monitor.setCursor(25, 5); // перевод курсора на 5 строку
  oled_monitor.print(pressurePascal);
  oled_monitor.println(" Паскалей");
  oled_monitor.setCursor(25, 6); // перевод курсора на 5 строку
  oled_monitor.print(pressureClassic);
  oled_monitor.println(" мм.р.ст.");
  
  // вывод климатических данных на сериал
  Serial.print("Температура ");
  Serial.print(temperature); // Выводим темперутуру в [*C]
  Serial.println(" градусов");
  Serial.print("Давление ");
  Serial.print(pressurePascal); // Выводим давление в Паскалях
  Serial.println(" Па.");
  Serial.print(pressureClassic); // Выводим давление в мм.рт.ст.
  Serial.println(" мм.рт.ст.");
  
  delay(10000); // задержка для экономии ресурсов котроллера
  }
}

// метод выполнения действий во время простоя метода loop()
void yield() {
  // приём данных со смартфона
  if(Serial.available()) { // если по порту пришли данные
    char input = Serial.read(); // считывание данных
    String info = ""; // информационная строка
    if(input == '1') {
        digitalWrite(LED_PIN_2, HIGH);
        info = "Включен 1 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'a') {
        digitalWrite(LED_PIN_2, LOW);
        info = "Выключен 1 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '2') {
        digitalWrite(LED_PIN_3, HIGH);
        info = "Включен 2 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'b') {
        digitalWrite(LED_PIN_3, LOW);
        info = "Выключен 2 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '3') {
        digitalWrite(LED_PIN_4, HIGH);
        info = "Включен 3 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'c') {
        digitalWrite(LED_PIN_4, LOW);
        info = "Выключен 3 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '4') {
        digitalWrite(LED_PIN_5, HIGH);
        info = "Включен 4 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'd') {
        digitalWrite(LED_PIN_5, LOW);
        info = "Выключен 4 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '5') {
        digitalWrite(LED_PIN_6, HIGH);
        info = "Включен 5 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'e') {
        digitalWrite(LED_PIN_6, LOW);
        info = "Выключен 5 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '6') {
        digitalWrite(LED_PIN_7, HIGH);
        info = "Включен 6 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'f') {
        digitalWrite(LED_PIN_7, LOW);
        info = "Выключен 6 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '7') {
        digitalWrite(LED_PIN_8, HIGH);
        info = "Включен 7 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'g') {
        digitalWrite(LED_PIN_8, LOW);
        info = "Выключен 7 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == '8') {
        digitalWrite(LED_PIN_9, HIGH);
        info = "Включен 8 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } else if(input == 'h') {
        digitalWrite(LED_PIN_9, LOW);
        info = "Выключен 8 светодиод";
        Serial.println(info);
        oled_monitor.print(info);
      } 
  }
}
