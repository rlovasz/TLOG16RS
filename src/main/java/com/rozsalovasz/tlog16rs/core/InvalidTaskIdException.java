package com.rozsalovasz.tlog16rs.core;

/**
 * This exception is thrown if the taskId has an invalid value
 *
 * @author rlovasz
 */
public class InvalidTaskIdException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public InvalidTaskIdException(String message) {
        super(message);
    }
}
