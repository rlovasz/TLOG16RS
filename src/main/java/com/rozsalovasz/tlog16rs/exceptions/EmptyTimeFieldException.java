package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This exception is thrown if there is an empty time argument in the created task
 *
 * @author rlovasz
 */
public class EmptyTimeFieldException extends IllegalArgumentException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public EmptyTimeFieldException(String message) {
        super(message);
    }
    
}
