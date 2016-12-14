/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.core.NotTheSameMonthException;
import com.rozsalovasz.tlog16rs.core.NotNewDateException;
import com.rozsalovasz.tlog16rs.core.WeekendNotEnabledException;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkMonthTest {
    
    @Test
    public void testGetSumPerMonthNormal() {
        Task task1 = new Task("1856", "This is a comment", 7, 30, 8, 45);
        Task task2 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        WorkDay workDay1 = new WorkDay(420, 2016, 9, 2);
        WorkDay workDay2 = new WorkDay(420, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay1.addTask(task1);
        workDay2.addTask(task2);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
        long expResult = 135;
        long result = workMonth.getSumPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetSumPerMonthNoDays() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        long expResult = 0;
        long result = workMonth.getSumPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetExtraMinPerMonthNormal() {
        Task task1 = new Task("1856", "This is a comment", 7, 30, 8, 45);
        Task task2 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        WorkDay workDay1 = new WorkDay(420, 2016, 9, 2);
        WorkDay workDay2 = new WorkDay(420, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay1.addTask(task1);
        workDay2.addTask(task2);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
        long expResult = -705;
        long result = workMonth.getExtraMinPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetExtraMinPerMonthNoDays() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        long expResult = 0;
        long result = workMonth.getExtraMinPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetRequiredMinPerMonthNormal() {
        WorkDay workDay1 = new WorkDay(420, 2016, 9, 2);
        WorkDay workDay2 = new WorkDay(420, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
        long expResult = 840;
        long result = workMonth.getRequiredMinPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetRequiredMinPerMonthNoDays() {        
        WorkMonth workMonth = new WorkMonth(2016, 9);
        long expResult = 0;
        long result = workMonth.getRequiredMinPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testAddWorkDayWeekday() {
        WorkDay workDay = new WorkDay(420, 2016, 9, 2);
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay.addTask(task);
        workMonth.addWorkDay(workDay);
        assertEquals(workDay.getSumPerDay(), workMonth.getSumPerMonth());
    }
    
    @Test
    public void testAddWorkDayWeekendTrue() {
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(2016, 9, 10);
        workDay.addTask(task);
        workMonth.addWorkDay(workDay, true);
        assertEquals(workDay.getSumPerDay(), workMonth.getSumPerMonth());
    }
    
    @Test(expected = WeekendNotEnabledException.class)
    public void testAddWorkDayWeekendFalse() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(2016, 9, 10);
        workMonth.addWorkDay(workDay);
    }
    
    @Test
    public void testIsSameMonthTrue() {
        WorkDay workDay = new WorkDay(400, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = true;
        boolean result = workMonth.isSameMonth(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsSameMonthFalse() {
        WorkDay workDay = new WorkDay(400, 2016, 8, 30);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = false;
        boolean result = workMonth.isSameMonth(workDay);
        assertEquals(expResult, result);
    }
    
     @Test
    public void testIsNewDateFalse() {
        WorkDay workDay1 = new WorkDay(2016, 9, 1);
        WorkDay workDay2 = new WorkDay(400, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = false;
        workMonth.addWorkDay(workDay1);
        boolean result = workMonth.isNewDate(workDay2);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsNewDateTrue() {
        WorkDay workDay1 = new WorkDay(2016, 9, 1);
        WorkDay workDay2 = new WorkDay(400, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = true;
        workMonth.addWorkDay(workDay1);
        boolean result = workMonth.isNewDate(workDay2);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsNewDateEmptyDays() {
        WorkDay workDay = new WorkDay(400, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = true;
        boolean result = workMonth.isNewDate(workDay);
        assertEquals(expResult, result);
    }    
    
    @Test(expected = NotNewDateException.class)
    public void testAddWorkDayNewFalse() {
        WorkDay workDay1 = new WorkDay(2016, 9, 1);
        WorkDay workDay2 = new WorkDay(400, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
    }
    
    @Test
    public void testAddWorkDayNewTrue() {
        WorkDay workDay1 = new WorkDay(2016, 9, 1);
        WorkDay workDay2 = new WorkDay(400, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
        assertEquals(workDay1.getRequiredMinPerDay() + workDay2.getRequiredMinPerDay(), workMonth.getRequiredMinPerMonth());
    }
    
    @Test(expected = NotTheSameMonthException.class)
    public void testAddWorkDaySameMonthFalse() {
        WorkDay workDay = new WorkDay(2016, 8, 30);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay);
    }

    @Test
    public void getHasDifferentYearValueTrue() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(400, 2015, 8, 28);
        boolean expResult = true;
        boolean result = workMonth.hasDifferentYearValue(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void getHasDifferentYearValueFalse() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(400, 2016, 8, 30);
        boolean expResult = false;
        boolean result = workMonth.hasDifferentYearValue(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void getHasDifferentMonthValueTrue() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(400, 2015, 8, 28);
        boolean expResult = true;
        boolean result = workMonth.hasDifferentMonthValue(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void getHasDifferentMonthValueFalse() {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(400, 2016, 9, 1);
        boolean expResult = false;
        boolean result = workMonth.hasDifferentMonthValue(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsSameMonthEmptyDaysTrue() {
        WorkDay workDay = new WorkDay(400, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = true;
        boolean result = workMonth.isSameMonth(workDay);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsSameMonthEmptyDaysFalse() {
        WorkDay workDay = new WorkDay(400, 2016, 8, 30);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        boolean expResult = false;
        boolean result = workMonth.isSameMonth(workDay);
        assertEquals(expResult, result);
    }
}
