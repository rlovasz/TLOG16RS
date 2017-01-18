/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.FutureWorkException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.exceptions.NotNewDateException;
import com.rozsalovasz.tlog16rs.exceptions.NotNewMonthException;
import com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.exceptions.NotTheSameMonthException;
import com.rozsalovasz.tlog16rs.exceptions.WeekendNotEnabledException;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {

    private User getUser() {
        return new User("Lovász Rózsa", "12345", "111");
    }

    private Task getTask() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("4654", "", 7, 30, 10, 30);
    }

    @Test
    public void testAddMonthNormal() throws FutureWorkException, NotSeparatedTaskTimesException, NotExpectedTimeOrderException, InvalidTaskIdException, NoTaskIdException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException {
        WorkDay workDay = new WorkDay(2016, 4, 14);
        WorkMonth workMonth = new WorkMonth(2016, 4);
        Task task = getTask();
        workDay.addTask(task);
        workMonth.addWorkDay(workDay);
        User user = getUser();
        user.addMonth(workMonth);
        assertEquals(task.getMinPerTask(), user.getMonths().get(0).getSumPerMonth());
    }

    @Test(expected = NotNewMonthException.class)
    public void testAddMonthNotNewMonth() throws NotNewMonthException {
        User user = getUser();
        WorkMonth workMonth1 = new WorkMonth(2016, 4);
        WorkMonth workMonth2 = new WorkMonth(2016, 4);
        user.addMonth(workMonth1);
        user.addMonth(workMonth2);
    }
}
