package com.rozsalovasz.tlog16rs.core;

/**
 * This type of Exception is thrown if the created day is already exists
 *
 * @author rlovasz
 */
public class NotNewDateException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NotNewDateException(String message) {
        super(message);
    }
    
}
