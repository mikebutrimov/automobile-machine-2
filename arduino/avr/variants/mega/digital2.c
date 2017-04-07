/*
 * digital2.c
 *
 *  Created on: 13. 2. 2014
 *      Author: Jan Dolinay
 *
 *  Alternate version of digital input/output for Arduino.
 *  Implementation of helper functions for digital I/O.
 *  These functions are called by inline functions such as
 *  digitalWrite2, defined in Arduino2.h
 *
 *
  This is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
 *
 */

// Standard headers for AVR
#include <avr/pgmspace.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>

#define	ARDUINO2_MAIN

#include "pins2_arduino.h"		// Definitions specific for selected board
#include "Arduino.h"
#include "arduino2.h"


// Internal worker for pinMode2. It is called if the pin or mode
// are not known at compile time.
// The code is the same as in the fast version, just disables interrupts
// before modifying the registers.
void internal_pinMode2(GPIO_pin_t pin, uint8_t mode)
{
	if ( mode == OUTPUT )
	{
		GPIO2_ATOMIC_BEGIN
			GPIO_DDR_REG(pin) |= GPIO_PIN_MASK(pin);
		GPIO2_ATOMIC_END
	}
	else
	{
		if ( mode == INPUT_PULLUP )
		{
			GPIO2_ATOMIC_BEGIN
				GPIO_DDR_REG(pin) &= ~GPIO_PIN_MASK(pin);
				GPIO_PORT_REG(pin) |= GPIO_PIN_MASK(pin);
			GPIO2_ATOMIC_END
		}
		else
		{
			// input mode without pull-up
			GPIO2_ATOMIC_BEGIN
				GPIO_DDR_REG(pin) &= ~GPIO_PIN_MASK(pin);
				GPIO_PORT_REG(pin) &= ~GPIO_PIN_MASK(pin);
			GPIO2_ATOMIC_END
		}
	}
}

// Internal worker for digitalRead2f.
// It is called if the pin in not known at compile time.
uint8_t internal_digitalRead2(GPIO_pin_t pin)
{
	if ((GPIO_PIN_REG(pin) & GPIO_PIN_MASK(pin)) != 0)
		return HIGH;
	else
		return LOW;
}

// Internal worker for digitalWrite2f.
// It is called if the pin or value are not known at compile time.
// The code is the same as in the fast version, just disables interrupts
// before modifying the registers.
void internal_digitalWrite2(GPIO_pin_t pin, uint8_t value)
{
	if ( value == 0 )
	{
		GPIO2_ATOMIC_BEGIN
			GPIO_PORT_REG(pin) &= ~GPIO_PIN_MASK(pin);
		GPIO2_ATOMIC_END
	 }
	 else
	 {
		 GPIO2_ATOMIC_BEGIN
			 GPIO_PORT_REG(pin) |= GPIO_PIN_MASK(pin);
		 GPIO2_ATOMIC_END
	 }
}
