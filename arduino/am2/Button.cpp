#include "Button.h"
#include "Arduino.h"
Button::Button(int pin){
  //LES CONSTRUTOR
  _pin = pin;
  _shortPressDelay = 5;
  _longPressDelay = 400;
  _buttonState = 0;
  _shortPressTimer = 0;
  _longPressTimer = 0; 
  pinMode(pin,INPUT);
}
int Button::getState(){
  int readout0 = digitalRead(_pin);
  delay(1);
  int readout1 = digitalRead(_pin);
  if (readout0 == readout1){
    return readout1;
  }
  else {
    return 0;
  }
}

int Button::processState(){
  int _state = getState();
  //check timer states
  if (_shortPressTimer == 0 && _longPressTimer == 0){
    //timers both are off, we start from full unpressed mode
    if (_buttonState == 0 && _state == 1){
      //button was not pressed and now reads as pressed
      //fire up both timers
      _shortPressTimer = millis();
      _longPressTimer = millis();
      _buttonState = 1;
    }
  }
  if (_shortPressTimer != 0 and _longPressTimer !=0 ){
    unsigned long currentTime = millis();
    if (currentTime > (_longPressTimer + _longPressDelay)){
      if (_buttonState == 3 && _state == 1){
        _buttonState = 2;
        
      }
      if (_buttonState > 0 && _state == 0){
        _longPressTimer = 0;
        _shortPressTimer = 0;
        _buttonState = 0;
      }
    }
  else if (currentTime > (_shortPressTimer + _shortPressDelay)){
      if (_buttonState > 0 && _state == 0){
        _longPressTimer = 0;
        _shortPressTimer = 0;
        _buttonState = 0;
      }
      else if (_buttonState == 1 && _state == 1 && 
                currentTime < (_longPressTimer + _longPressDelay)){
        _buttonState = 3;
      }
    } 
  }
//yiu can use it to tune reaction on type 3 press
/*if (_buttonState == 3){
  return 1;
}*/
return _buttonState;
}
