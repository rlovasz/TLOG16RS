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
import com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkDayTest {

   private WorkDay getNormalWorkDayWithDate() throws NegativeMinutesOfWorkException, FutureWorkException {
        return new WorkDay(2016, 9, 1);
    }
    
    private WorkDay getNormalWorkDayWithRequiredMinPerDay() throws NegativeMinutesOfWorkException, FutureWorkException {
        return new WorkDay(350);
    }
    
    private WorkDay getNormalWorkDayWithDefaultValues() throws NegativeMinutesOfWorkException, FutureWorkException {
        return new WorkDay();
    }
    
    private WorkDay getNormalWorkDayWithGivenValues() throws NegativeMinutesOfWorkException, FutureWorkException {
        return new WorkDay(300, 2016, 9, 1);
    }
    
    private Task getNormalTask1() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("7894", "comment", "07:30", "08:15");
    }
    
    private Task getNormalTask2() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("7777", "comment", "08:15", "08:45");
    }

    @Test
    public void testGetExtraMinPerDayNegative() throws NegativeMinutesOfWorkException, FutureWorkException, InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException {
        WorkDay workDay = getNormalWorkDayWithGivenValues();
        Task task = getNormalTask1();
        workDay.addTask(task);
        long expResult = -255;
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }
    
    @Test()
    public void testGetExtraMinPerDayNoTask() throws NegativeMinutesOfWorkException, FutureWorkException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        long expResult = (-1) * workDay.getRequiredMinPerDay();
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }
    
    @Test(expected = NegativeMinutesOfWorkException.class)
    public void testSetRequiredMinPerDayNegative() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        workDay.setRequiredMinPerDay(-15);
    }
    
    @Test
    public void testSetRequiredMinPerDayNormal() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithDate();
        workDay.setRequiredMinPerDay(200);
        assertEquals(200, workDay.getRequiredMinPerDay());
    }
    
    @Test(expected = NegativeMinutesOfWorkException.class)
    public void testWorkDayNegativeRequiredMin() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = new WorkDay(-100);
    }
    
    @Test(expected = FutureWorkException.class)
    public void testSetActualDayFuture() throws NegativeMinutesOfWorkException, FutureWorkException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        WorkDay workDay = new WorkDay(2016, 9, 1);
        workDay.setActualDay(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
    }
    
    @Test
    public void testSetActualDayNormal() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithDate();
        workDay.setActualDay(2017, 1, 6);
        assertEquals(LocalDate.of(2017, 1, 6), workDay.getActualDay());
    }
    
    @Test(expected = FutureWorkException.class)
    public void testWorkDayFuture() throws NegativeMinutesOfWorkException, FutureWorkException {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        WorkDay workDay = new WorkDay(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
    }
    
    @Test
    public void testGetSumPerDayNormal() throws NegativeMinutesOfWorkException, FutureWorkException, InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException {
        WorkDay workDay = getNormalWorkDayWithDate();
        Task task1 = getNormalTask1();
        Task task2 = getNormalTask2();
        workDay.addTask(task1);
        workDay.addTask(task2);
        long expResult = 75;
        long result = workDay.getSumPerDay();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetSumPerDayNoTask() throws NegativeMinutesOfWorkException, FutureWorkException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        long expResult = 0;
        long result = workDay.getSumPerDay();
        assertEquals(expResult, result);
    }
    
    @Test(expected = NotSeparatedTaskTimesException.class)
    public void testAddTaskNotSeparatedTimes() throws NegativeMinutesOfWorkException, FutureWorkException, InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException {
        WorkDay workDay = getNormalWorkDayWithDate();
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "09:00", "10:30");
        workDay.addTask(task1);
        workDay.addTask(task2);
    }
    
    @Test(expected = NotSeparatedTaskTimesException.class)
    public void testAddTaskNotSeparatedTimesWithRound() throws NegativeMinutesOfWorkException, FutureWorkException, InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException {
        WorkDay workDay = getNormalWorkDayWithDate();
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 50);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "08:20", "08:45");
        workDay.addTask(task1);
        workDay.addTask(task2);
    }
    
    @Test
    public void testCreateNormalWorkDayWithGivenValues() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithGivenValues();
        assertEquals(LocalDate.of(2016, 9, 1), workDay.getActualDay());
        assertEquals(300, workDay.getRequiredMinPerDay());
    }
    
    @Test
    public void testCreateNormalWorkDayWithDate() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithDate();
        assertEquals(LocalDate.of(2016, 9, 1), workDay.getActualDay());
        assertEquals(450, workDay.getRequiredMinPerDay());
    }
    
    @Test
    public void testCreateNormalWorkDayWithRequiredMinPerDay() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithRequiredMinPerDay();
        assertEquals(LocalDate.now(), workDay.getActualDay());
        assertEquals(350, workDay.getRequiredMinPerDay());
    }
    
    @Test
    public void testCreateNormalWorkDayWithDefaultValues() throws NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = getNormalWorkDayWithDefaultValues();
        assertEquals(LocalDate.now(), workDay.getActualDay());
        assertEquals(450, workDay.getRequiredMinPerDay());
    }
    
    @Test(expected = EmptyTimeFieldException.class)
    public void testAddTaskWithOnlyTaskId() throws InvalidTaskIdException, NoTaskIdException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException {
        Task task = new Task("4875");
        WorkDay workDay = getNormalWorkDayWithDate();
        workDay.addTask(task);
        workDay.getSumPerDay();
    }
}
