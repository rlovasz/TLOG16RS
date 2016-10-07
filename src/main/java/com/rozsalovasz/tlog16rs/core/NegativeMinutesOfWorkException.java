package com.rozsalovasz.tlog16rs.core;

/**
 * This exception is thrown if the required working hours of day if set to be negative
 *
 * @author rlovasz
 */
public class NegativeMinutesOfWorkException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NegativeMinutesOfWorkException(String message) {
        super(message);
    }
}
