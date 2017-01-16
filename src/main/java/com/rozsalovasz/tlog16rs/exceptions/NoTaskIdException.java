package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This type of Exceptions is thrown if the task has no set taskId
 *
 * @author rlovasz
 */
public class NoTaskIdException extends Exception {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NoTaskIdException(String message) {
        super(message);
    }
    
}
