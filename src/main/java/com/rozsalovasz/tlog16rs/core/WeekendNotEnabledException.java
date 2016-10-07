package com.rozsalovasz.tlog16rs.core;

/**
 * Exception type, which is thrown if a work day is on weekend and it is not enabled to work on weekend
 *
 * @author rlovasz
 */
public class WeekendNotEnabledException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public WeekendNotEnabledException(String message) {
        super(message);
    }
    
}
