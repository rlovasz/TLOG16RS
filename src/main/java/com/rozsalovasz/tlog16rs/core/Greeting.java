/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author precognox
 */
public class Greeting {
    @JsonProperty
    private String greeting;
    
    public Greeting(String greeting){
    this.greeting = greeting;
    }
    
    public String getGreeting()
    {
    return greeting;
    }
}
