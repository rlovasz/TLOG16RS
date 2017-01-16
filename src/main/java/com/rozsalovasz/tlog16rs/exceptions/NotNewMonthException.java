package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This type of Exceptions is thrown if the created month already exists
 *
 * @author rlovasz
 */
public class NotNewMonthException extends Exception {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NotNewMonthException(String message) {
         super(message);
    }
    
}
