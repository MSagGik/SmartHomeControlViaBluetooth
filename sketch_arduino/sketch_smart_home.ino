// библиотеки
#include <SPI.h>                // стандартная (https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/SPI, Security policy)
#include <GyverBME280.h>        // сенсора BME280 (https://github.com/GyverLibs/GyverBME280, MIT license)
#include <DHT.h>                // сенсора DHT11 (https://github.com/adafruit/DHT-sensor-library, MIT license)
#include <GyverOLED.h>          // OLED экрана (https://github.com/GyverLibs/GyverOLED, MIT license)
#include <RtcDS1302.h>          // часов (https://github.com/Makuna/Rtc, LGPL-3.0 license)
#include <ThreeWire.h>          // вспомогательная для часов (https://github.com/Makuna/Rtc, LGPL-3.0 license)
#include <Adafruit_NeoPixel.h>  // светодиодной ленты (https://github.com/adafruit/Adafruit_NeoPixel, LGPL-3.0 license)
#include <SmartHome.h>          // разработанная
// пины
#define PIN_2 2  // часы DAT (ввод/вывод)
#define PIN_3 3  // часы CLK (кварцевый генератор)
#define PIN_4 4  // часы RST (сессия)
#define PIN_5 5  // датчик DHT11
#define PIN_6 6  // светодиодная лента
#define PIN_7 7  // динамик звуковых сигналов
// константы
#define DHT_SENSOR DHT11  // сенсор DHT11
#define NUMBER_LED 141    // количество светодиодов
const static uint8_t arrayImage[][8] PROGMEM = { // данные библиотеки GyverOLED (https://github.com/GyverLibs/GyverOLED, MIT license)
  { 0x1e, 0x3f, 0x7f, 0xfe, 0xfe, 0x7f, 0x3f, 0x1e },  // сердце (заполненное)
  { 0x1e, 0x21, 0x41, 0x82, 0x82, 0x41, 0x21, 0x1e },  // сердце (контур)
  { 0x10, 0x30, 0x70, 0xff, 0xff, 0x70, 0x30, 0x10 },  // стрелка вниз
  { 0x08, 0x0c, 0x0e, 0xff, 0xff, 0x0e, 0x0c, 0x08 },  // стрелка вверх
  { 0x06, 0x09, 0x09, 0x06, 0x78, 0x84, 0x84, 0x48 },  // градус Цельсия
  { 0xf8, 0x84, 0x82, 0x81, 0xb1, 0xb2, 0x84, 0xf8 },  // дом
};
// объекты
ThreeWire clockLink(PIN_2, PIN_3, PIN_4);                                // выводы часов DAT, CLK, RST
RtcDS1302<ThreeWire> clock(clockLink);                                   // модуль часов
GyverBME280 sensorBME280;                                                // сенсор BME280 (температура и давление)
DHT sensorDHT11(PIN_5, DHT_SENSOR);                                      // сенсор DHT11 (температура и влажность)
Adafruit_NeoPixel lightingLED(NUMBER_LED, PIN_6, NEO_GRB + NEO_KHZ800);  // светодиодная лента
static int argb[4] = { 0, 0, 0, 0 };                                     // массив параметров светодиодной ленты (alfa, red, green, blue)
GyverOLED<SSD1306_128x64, OLED_NO_BUFFER> monitorLED;                    // светодиодный монитор 128x64

// метод выполняемый на старте и финише работы контроллера
void setup() {
  Serial.begin(9600);  // запуск порта
  // загрузка даты и времени с ПК при прошивке
  clock.Begin();  // инициализация часов
  // Serial.println(__DATE__);                                // дата
  // Serial.println(__TIME__);                                // время
  // RtcDateTime realTime = RtcDateTime(__DATE__, __TIME__);  // формат времени
  // clock.SetDateTime(realTime);                             // Установка времени
  // сенсоры
  sensorBME280.begin();  // инициализация сенсора (температура и давление)
  sensorDHT11.begin();   // инициализация сенсора (температура и влажность)
  // светодиодная лента
  lightingLED.begin();                 // инициализируем ленту
  lightingLED.setBrightness(argb[0]);  // указываем яркость светодиодов (максимум 255)
  // звуковой вывод
  pinMode(PIN_7, OUTPUT);
  // светодиодный экран
  monitorLED.init();       // инициализация
  monitorLED.clear();      // очистка
  monitorLED.setScale(1);  // масштаб текста (1..4)
  monitorLED.home();       // курсор в 0,0
  monitorLED.print("Умный дом 1.0 ");
  draw(5);
  monitorLED.setCursor(0, 3);  // перевод курсора на 3 строку
  monitorLED.println("Данные температуры,");
  monitorLED.println("давления и влажности");
  monitorLED.clear();  // очистка
}

// цикличный метод запускаемый после метода setup()
void loop() {
  RtcDateTime updateTime;
  char time[10];
  if (!Serial.available()) {  // если порт свободен
    // часы
    for (int i = 0; i < 10; i++) {
      delay(1000);
      updateTime = clock.GetDateTime();  // обновление данных с модуля времени
      // вывод времни на экран
      monitorLED.setScale(2);  // масштаб текста (1..4)
      monitorLED.home();       // курсор в 0,0
      sprintf(time, "%02d:%02d:%02d", updateTime.Hour(), updateTime.Minute(), updateTime.Second());
      monitorLED.print(time);                                                                // часы
      if (updateTime.Minute() == 0 && updateTime.Second() == 0) outputSoundOne(PIN_7, 180);  // запуск мелодии на динамике в начале часа
      monitorLED.setScale(1);                                                                // масштаб текста (1..4)
      monitorLED.setCursor(109, 0);
      monitorLED.print(updateTime.Day());
      switch (updateTime.Month()) {
        case 1:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Янв");
          break;
        case 2:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Фев");
          break;
        case 3:
          monitorLED.setCursor(99, 1);
          monitorLED.print("Марта");
          break;
        case 4:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Апр");
          break;
        case 5:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Мая");
          break;
        case 6:
          monitorLED.setCursor(99, 1);
          monitorLED.print("Июня");
          break;
        case 7:
          monitorLED.setCursor(99, 1);
          monitorLED.print("Июля");
          break;
        case 8:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Авг");
          break;
        case 9:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Сен");
          break;
        case 10:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Окт");
          break;
        case 11:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Ноя");
          break;
        case 12:
          monitorLED.setCursor(107, 1);
          monitorLED.print("Дек");
          break;
      }
      monitorLED.setCursor(102, 2);
      monitorLED.print(updateTime.Year());
    }
    // считывание данных сенсора
    float pressurePascalBME280 = sensorBME280.readPressure();              // считывание давления в Паскалях
    float pressureToMmHgBME280 = pressureToMmHg(pressurePascalBME280);     // конвертирование давления в мм.рт.ст.
    float temperatureBME280 = sensorBME280.readTemperature();              // считывание температуры в градусах
    float temperatureDHT11 = sensorDHT11.readTemperature();                // считывание температуры в градусах
    float temperatureMedian = (temperatureBME280 + temperatureDHT11) / 2;  // усреднение температуры в градусах
    float humidityDHT11 = sensorDHT11.readHumidity();                      // считывание влажности в %
    // вывод климатических данных на монитор
    monitorLED.setScale(1);  // масштаб текста (1..4)
    monitorLED.setCursor(0, 2);
    monitorLED.print("Метеоданные");
    monitorLED.setCursor(0, 4);  // перевод курсора на 4 строку
    monitorLED.setScale(1);      // масштаб текста (1..4)
    monitorLED.print(temperatureMedian);
    draw(4);
    monitorLED.print(" ");
    Serial.print("Время ");
    Serial.print(time);
    Serial.print(" ");
    Serial.print(updateTime.Day());
    Serial.print(".");
    Serial.print(updateTime.Month());
    Serial.print(".");
    Serial.println(updateTime.Year());
    Serial.print("Температура ");
    Serial.print(temperatureMedian);
    Serial.print(" *С ");
    if (temperatureMedian > 24.0) {
      draw(3);
      Serial.println("(высокая)");
    } else if (temperatureMedian < 18.0) {
      draw(2);
      Serial.println("(низкая)");
    } else {
      draw(0);
      Serial.println("(нормальная))");
    }
    monitorLED.print("   ");
    monitorLED.print(humidityDHT11);
    monitorLED.print("%");
    monitorLED.print(" ");
    Serial.print("Влажность ");
    Serial.print(humidityDHT11);
    Serial.print(" % ");
    if (humidityDHT11 > 60.0) {
      draw(3);
      Serial.println("(высокая)");
    } else if (humidityDHT11 < 40.0) {
      draw(2);
      Serial.println("(низкая)");
    } else {
      draw(0);
      Serial.println("(нормальная))");
    }
    monitorLED.setCursor(0, 6);  // перевод курсора на 6 строку
    monitorLED.print(pressureToMmHgBME280);
    monitorLED.print(" мм.р.ст. ");
    Serial.print("Давление ");
    Serial.print(pressureToMmHgBME280);
    Serial.print("  мм.р.ст. ");
    if (pressureToMmHgBME280 > 760.0) {
      draw(3);
      Serial.println("(высокое)");
    } else if (pressureToMmHgBME280 < 747.0) {
      draw(2);
      Serial.println("(низкое)");
    } else {
      draw(0);
      Serial.println("(нормальное)");
    }
    Serial.println("");
    Serial.flush();
  }
}

// функция отрисовки картинок
void draw(byte index) {
  size_t s = sizeof arrayImage[index];
  for (unsigned int i = 0; i < s; i++) {
    monitorLED.drawByte(pgm_read_byte(&(arrayImage[index][i])));
  }
}

// функция обновления цвета светодиодной ленты
void updateColorLightingLED(int number, int argbInput[]) {
  for (int i = 0; i < number; i++) {
    lightingLED.setPixelColor(i, lightingLED.Color(argbInput[1], argbInput[2], argbInput[3]));  // задание цвета на каждом светододе
  }
  lightingLED.show();  // отправляем сигнал на ленту
}

// функция обновления прозрачности и цвета светодиодной ленты
void updateAlfaColorLightingLED(int number, int argbInput[]) {
  lightingLED.setBrightness(argbInput[0]);
  updateColorLightingLED(number, argbInput);
}

// метод выполнения действий во время простоя метода loop()
void yield() {
  // приём данных со смартфона
  if (Serial.available()) {      // если по порту пришли данные
    char input = Serial.read();  // считывание данных
    String info = "";            // информационная строка
    switch (input) {
      case '0':               // выключить светодиодную ленту
        lightingLED.clear();  // очистка параметров
        lightingLED.show();   // обновление цвета ленты
        info = "Светодиодная лента выключена";
        break;
      case '1':  // выключить светодиодную ленту
        if (argb[1] == 0 && argb[2] == 0 && argb[3] == 0) {
          if (argb[0] == 0) argb[0] = 3;
          argb[1] = 100;
          argb[2] = 100;
          argb[3] = 100;
        }
        updateAlfaColorLightingLED(NUMBER_LED, argb);
        info = "Светодиодная лента включена";
        break;
      case '2':  // выбрать предустановленную программу цветовой гаммы
        info = "Данная функция в разработке";
        break;
      case '3':                       // установить новую цветовую гамму
        argb[1] = Serial.parseInt();  // считывание красного цвета
        argb[2] = Serial.parseInt();  // считывание зелённого цвета
        argb[3] = Serial.parseInt();  // считывание синего цвета
        updateColorLightingLED(NUMBER_LED, argb);
        info = "Установлен новый цвет светодиодной ленты";
        break;
      case '4':  // сложная настройка цветoвой гаммы
        info = "Данная функция в разработке";
        break;
      case '5':                       // настроить яркость
        argb[0] = Serial.parseInt();  // считывание яркости
        updateAlfaColorLightingLED(NUMBER_LED, argb);
        info = "Установлена яркость светодиодной ленты";
        break;
      case '6':  // обновить время часов
        info = "Данная функция в разработке";
        break;
      case '7':  // установить напоминание в календарь
        info = "Данная функция в разработке";
        break;
      case '8':  //  настройки
        info = "Данная функция в разработке";
        break;
      case '9':  //  считать цвет светодиодной ленты
        Serial.print("a ");
        Serial.print(argb[0]);
        Serial.print(", r ");
        Serial.print(argb[1]);
        Serial.print(", g ");
        Serial.print(argb[2]);
        Serial.print(", b ");
        Serial.println(argb[3]);
        break;
    }
    if (info != "") Serial.println(info);
    Serial.flush();
  }
}