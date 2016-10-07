package com.rozsalovasz.tlog16rs.core;
/**
 * This exception type is thrown if the working day is added to a month which should not be its containing month by the date of the day
 *
 * @author rlovasz
 */
public class NotTheSameMonthException extends RuntimeException {

	/**
	 *
	 * @param message sets the message of the exception
	 */
    public NotTheSameMonthException(String message) {
        super(message);
    }
    
}
