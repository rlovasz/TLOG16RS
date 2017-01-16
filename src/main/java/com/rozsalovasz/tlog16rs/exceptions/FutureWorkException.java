package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This exception is thrown if the created day's date is later than today
 *
 * @author rlovasz
 */
public class FutureWorkException extends Exception {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public FutureWorkException(String message) {
        super(message);
    }
    
}
