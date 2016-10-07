package com.rozsalovasz.tlog16rs.core;

/**
 * This exception is thrown if the task's time interval is not multiple of quarter hour
 *
 * @author rlovasz
 */
public class NotMultipleQuarterHourException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NotMultipleQuarterHourException(String message) {
        super(message);
    }
}
