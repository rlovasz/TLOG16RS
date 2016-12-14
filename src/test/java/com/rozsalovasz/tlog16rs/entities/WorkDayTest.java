/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.core.FutureWorkException;
import com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.core.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkDayTest {

    @Test
    public void testGetExtraMinPerDayZero(){
        WorkDay workDay = new WorkDay(75, 2016, 9, 1);
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        workDay.addTask(task);
        long expResult = 0;
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetExtraMinPerDayNegative() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        workDay.addTask(task);
        long expResult = -375;
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetExtraMinPerDayPositive() {
        WorkDay workDay = new WorkDay(60,2016, 9, 1);
        Task task = new Task("1856", "This is a comment", 7, 30, 8, 45);
        workDay.addTask(task);
        long expResult = 15;
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }

    @Test()
    public void testGetExtraMinPerDayNoTask() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        long expResult = (-1) * workDay.getRequiredMinPerDay();
        long result = workDay.getExtraMinPerDay();
        assertEquals(expResult, result);
    }

    @Test(expected = NegativeMinutesOfWorkException.class)
    public void testSetRequiredMinPerDayNegative() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        workDay.setRequiredMinPerDay(-15);
    }

    @Test(expected = NegativeMinutesOfWorkException.class)
    public void testWorkDayNegativeRequiredMin() {
        WorkDay workDay = new WorkDay(-100);
    }

    @Test(expected = FutureWorkException.class)
    public void testSetActualDayFuture() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        WorkDay workDay = new WorkDay(2016, 9, 1);
        workDay.setActualDay(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
    }

    @Test(expected = FutureWorkException.class)
    public void testWorkDayFuture(){
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        WorkDay workDay = new WorkDay(tomorrow.getYear(), tomorrow.getMonthValue(), tomorrow.getDayOfMonth());
    }

    @Test
    public void testGetSumPerDayNormal() {
        WorkDay workDay = new WorkDay(60, 2016, 9, 1);
        Task task1 = new Task("1856", "This is a comment", 7, 30, 8, 45);
        Task task2 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        workDay.addTask(task1);
        workDay.addTask(task2);
        long expResult = 135;
        long result = workDay.getSumPerDay();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetSumPerDayNoTask() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        long expResult = 0;
        long result = workDay.getSumPerDay();
        assertEquals(expResult, result);
    }

    @Test
    public void testAddTaskNormal() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task = new Task("1486", "This is a comment", 8, 45, 9, 45);
        workDay.addTask(task);
        assertEquals(task.getMinPerTask(), workDay.getSumPerDay());
    }

    @Test(expected = NotMultipleQuarterHourException.class)
    public void testAddTaskNotMultipleQuarterHour(){
        Task task = new Task("1234", "", 7, 10, 8, 20);
        WorkDay workDay = new WorkDay(2016, 9, 1);
        workDay.addTask(task);
    }

    @Test(expected = NotSeparatedTaskTimesException.class)
    public void testAddTaskNotSeparatedTimes() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "09:00", "10:30");
        workDay.addTask(task1);
        workDay.addTask(task2);
    }

    @Test
    public void testIsSeparatedTimeTrue() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "09:45", "10:30");
        workDay.addTask(task1);
        boolean expResult = true;
        boolean result = workDay.isSeparatedTime(task2);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsSeparatedTimeFalseT1(){
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "09:00", "10:30");
        workDay.addTask(task1);
        boolean expResult = false;
        boolean result = workDay.isSeparatedTime(task2);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsSeparatedTimeFalseT2() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "09:00", "10:30");
        workDay.addTask(task2);
        boolean expResult = false;
        boolean result = workDay.isSeparatedTime(task1);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsSeparatedTimeFalseT3() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        Task task1 = new Task("1486", "This is a comment", 8, 45, 9, 45);
        Task task2 = new Task("4823", "flkhflks ylskjféky", "08:45", "09:45");
        workDay.addTask(task1);
        boolean expResult = false;
        boolean result = workDay.isSeparatedTime(task2);
        assertEquals(expResult, result);
    }

    @Test
    public void testIsWeekdayTrue() {
        WorkDay workDay = new WorkDay(2016, 9, 1);
        boolean expResult = true;
        boolean result = workDay.isWeekday();
        assertEquals(expResult, result);
    }

    @Test
    public void testIsWeekdayFalse() {
        WorkDay workDay = new WorkDay(2016, 9, 10);
        boolean expResult = false;
        boolean result = workDay.isWeekday();
        assertEquals(expResult, result);
    }
}
