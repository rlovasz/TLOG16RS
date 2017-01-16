package com.rozsalovasz.tlog16rs.exceptions;

/**
 * This exception is thrown if the time has an invalid value
 *
 * @author rlovasz
 */
public class NotValidTimeExpressionException extends IllegalArgumentException {

	public NotValidTimeExpressionException(String msg) {
		super(msg);
	}
}
