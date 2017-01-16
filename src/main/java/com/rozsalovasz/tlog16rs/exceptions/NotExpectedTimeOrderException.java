package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This exception type is thrown if the task ends earlier than it begins
 *
 * @author rlovasz
 */
public class NotExpectedTimeOrderException extends Exception {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NotExpectedTimeOrderException(String message) {
        super(message);
    }
    
}
