#ifndef Button_h
#define Button_h


class Button {
  public:
    Button(int pin);
    int getState();
    int processState();
  private:
    int _pin;
    int _shortPressDelay;
    int _longPressDelay;
    int _buttonState; //0 - released; 1 - short press; 2 - in long press; 
                      //3 - was in short press and now waits for long or release
    unsigned long  _shortPressTimer;
    unsigned long  _longPressTimer;
};

#endif
