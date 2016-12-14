/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.core.NotNewMonthException;
import org.junit.Test;
import static org.junit.Assert.*;

public class TimeLoggerTest {

    private TimeLogger getTimeLogger() {
        return new TimeLogger("Lovász Rózsa","12345", "dfjdsf");
    }

    private Task getTask() {
        Task task = new Task("4654", "", 7, 30, 10, 30);
        return task;
    }

    @Test
    public void testAddMonthNormal() {
        WorkDay workDay = new WorkDay(2016, 4, 14);
        WorkMonth workMonth = new WorkMonth(2016, 4);
        workDay.addTask(getTask());
        workMonth.addWorkDay(workDay);
        TimeLogger timeLogger = getTimeLogger();
        timeLogger.addMonth(workMonth);
        assertEquals(getTask().getMinPerTask(), timeLogger.getMonths().get(0).getSumPerMonth());
    }

    @Test(expected = NotNewMonthException.class)
    public void testAddMonthNotNewMonth() {
        TimeLogger timeLogger = getTimeLogger();
        WorkMonth workMonth1 = new WorkMonth(2016, 4);
        WorkMonth workMonth2 = new WorkMonth(2016, 4);
        timeLogger.addMonth(workMonth1);
        timeLogger.addMonth(workMonth2);
    }

    @Test
    public void testIsNewMonthTrue() {
        TimeLogger timeLogger = getTimeLogger();
        WorkMonth workMonth1 = new WorkMonth(2016, 4);
        WorkMonth workMonth2 = new WorkMonth(2016, 9);
        boolean expResult = true;
        timeLogger.addMonth(workMonth1);
        boolean result = timeLogger.isNewMonth(workMonth2);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsNewMonthFalse() {
        TimeLogger timeLogger = getTimeLogger();
        WorkMonth workMonth1 = new WorkMonth(2016, 4);
        WorkMonth workMonth2 = new WorkMonth(2016, 4);
        boolean expResult = false;
        timeLogger.addMonth(workMonth1);
        boolean result = timeLogger.isNewMonth(workMonth2);
        assertEquals(expResult, result);
    }

}
