/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.FutureWorkException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.exceptions.NotTheSameMonthException;
import com.rozsalovasz.tlog16rs.exceptions.NotNewDateException;
import com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.exceptions.WeekendNotEnabledException;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkMonthTest {
    
    @Test
    public void testGetSumPerMonthNormal() throws InvalidTaskIdException, NoTaskIdException, FutureWorkException, NotSeparatedTaskTimesException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
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
    
    @Test(expected = EmptyTimeFieldException.class)
    public void testGetSumPerMonthOnlyTaskIds() throws InvalidTaskIdException, NoTaskIdException, FutureWorkException, NotSeparatedTaskTimesException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        Task task1 = new Task("1856");
        WorkDay workDay1 = new WorkDay(420, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay1.addTask(task1);
        workMonth.addWorkDay(workDay1);
        workMonth.getSumPerMonth();
    }
    
    @Test
    public void testGetSumPerMonthNoDays() throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        long expResult = 0;
        long result = workMonth.getSumPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetExtraMinPerMonthNormal() throws InvalidTaskIdException, NoTaskIdException, NegativeMinutesOfWorkException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
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
    
    @Test(expected = EmptyTimeFieldException.class)
    public void testGetExtraMinPerMonthOnlyTaskIds() throws InvalidTaskIdException, NoTaskIdException, NegativeMinutesOfWorkException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        Task task1 = new Task("1856");
        WorkDay workDay1 = new WorkDay(420, 2016, 9, 2);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay1.addTask(task1);
        workMonth.addWorkDay(workDay1);
        workMonth.getExtraMinPerMonth();
    }
    
    @Test
    public void testGetExtraMinPerMonthNoDays() throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        long expResult = 0;
        long result = workMonth.getExtraMinPerMonth();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetRequiredMinPerMonthNormal() throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
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
    public void testAddWorkDayWeekday() throws NegativeMinutesOfWorkException, FutureWorkException, InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        WorkDay workDay = new WorkDay(420, 2016, 9, 2);
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workDay.addTask(task);
        workMonth.addWorkDay(workDay);
        assertEquals(workDay.getSumPerDay(), workMonth.getSumPerMonth());
    }
    
    @Test
    public void testAddWorkDayWeekendTrue() throws InvalidTaskIdException, NoTaskIdException, NegativeMinutesOfWorkException, FutureWorkException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(2016, 9, 10);
        workDay.addTask(task);
        workMonth.addWorkDay(workDay, true);
        assertEquals(workDay.getSumPerDay(), workMonth.getSumPerMonth());
    }
    
    @Test(expected = WeekendNotEnabledException.class)
    public void testAddWorkDayWeekendFalse() throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        WorkMonth workMonth = new WorkMonth(2016, 9);
        WorkDay workDay = new WorkDay(2016, 9, 10);
        workMonth.addWorkDay(workDay);
    } 
    
    @Test(expected = NotNewDateException.class)
    public void testAddWorkDayNewFalse() throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        WorkDay workDay1 = new WorkDay(2016, 9, 1);
        WorkDay workDay2 = new WorkDay(400, 2016, 9, 1);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay1);
        workMonth.addWorkDay(workDay2);
    }
    
    @Test(expected = NotTheSameMonthException.class)
    public void testAddWorkDaySameMonthFalse() throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        WorkDay workDay = new WorkDay(2016, 8, 30);
        WorkMonth workMonth = new WorkMonth(2016, 9);
        workMonth.addWorkDay(workDay);
    }
}
