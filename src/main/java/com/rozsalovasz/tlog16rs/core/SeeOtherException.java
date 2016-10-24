/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.core;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

/**
 *
 * @author precognox
 */
public class SeeOtherException extends ClientErrorException {

	public SeeOtherException(String message, Response.Status status) {
		super(message, Response.Status.SEE_OTHER);
	}

	
}
