// библиотеки
#include <SPI.h> // стандартная (https://github.com/arduino/ArduinoCore-avr/tree/master/libraries/SPI, Security policy)
#include <SmartHome.h> // текущая

// функция воспроизведения мелодии №1
void outputSoundOne(int pin, int tempoMelody) {
  int arrayNote[] = { NOTE_5, 8 };
  outputSound(arrayNote, pin, tempoMelody);
}

// функция воспроизведения мелодии №2
void outputSoundTwo(int pin, int tempoMelody) {
  int arrayNote[] = {
    NOTE_1, 3, NOTE_2, 3, NOTE_3, 3, NOTE_4, 3, NOTE_5, 3, NOTE_6, 3, NOTE_7, 3, NOTE_8, 3, NOTE_9, 3, NOTE_10, 3
  };
  outputSound(arrayNote, pin, tempoMelody);
}

// функция воспроизведения мелодии
void outputSound(int arrayNote[], int pin, int tempoMelody) {
  int sizeArrayMelody = sizeof(arrayNote) / sizeof(arrayNote[0]); // размер массива
  int delayMelody = 240000 / tempoMelody; // время задержки в миллисекундах
  int noteLength = 0, noteDeltaTime = 0; // относительная и абсолютная длины нот
  for (int i = 0; i < sizeArrayMelody; i += 2) {
    noteLength = arrayNote[i + 1];
    noteDeltaTime = delayMelody / noteLength;
    tone(pin, arrayNote[i], noteDeltaTime); // воспроизведение ноты
    delay(noteDeltaTime); // задержка
    noTone(pin); // приостановка воспроизведения ноты
  }
}