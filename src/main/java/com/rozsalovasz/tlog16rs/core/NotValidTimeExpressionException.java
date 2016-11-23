package com.rozsalovasz.tlog16rs.core;

/**
 * This exception is thrown if the time has an invalid value
 *
 * @author rlovasz
 */
public class NotValidTimeExpressionException extends RuntimeException {

	public NotValidTimeExpressionException(String msg) {
		super(msg);
	}
}
