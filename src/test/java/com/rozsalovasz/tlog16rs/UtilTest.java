/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs;

import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.entities.Task;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author precognox
 */
public class UtilTest {
    
    @Test
    public void testRoundToMultipleQuarterHour() {
        LocalTime result = Util.roundToMultipleQuarterHour(LocalTime.of(7,30), LocalTime.of(7,50));
        LocalTime expectedResult = LocalTime.of(7, 45);
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIsMultipleQuarterHourTrue() throws Exception {
        boolean result = Util.isMultipleQuarterHour(LocalTime.of(7,30), LocalTime.of(7,45));
        boolean expectedResult = true;
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIsMultipleQuarterHourFalse() throws Exception {
        boolean result = Util.isMultipleQuarterHour(LocalTime.of(7,30), LocalTime.of(7,50));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
    @Test(expected = EmptyTimeFieldException.class)
    public void testIsMultipleQuarterHourEmptyTimeField() throws Exception {
        Util.isMultipleQuarterHour(null, LocalTime.of(7,50));
    }
    
    @Test(expected = NotExpectedTimeOrderException.class)
    public void testIsMultipleQuarterHourReverseOrder() throws Exception {
        Util.isMultipleQuarterHour(LocalTime.of(8, 30), LocalTime.of(7,50));
    }

    @Test
    public void testIsSeparatedTimeTrueType1() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "06:45"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "05:30", "06:30"));
        boolean expectedResult = true;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeTrueType2() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "06:45"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:45", "07:00"));
        boolean expectedResult = true;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeTrueType3() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "06:30"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "05:30", "06:30"));
        boolean expectedResult = true;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeTrueType4() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:30"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "07:30", "07:30"));
        boolean expectedResult = true;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeFalseType1() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:00"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:00", "06:45"));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeFalseType2() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:00"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:30", "06:45"));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeFalseType3() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:00"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:45", "07:15"));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeFalseType4() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:00"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:45", "07:00"));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
    @Test
    public void testIsSeparatedTimeFalseType5() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        List<Task> tasks = new ArrayList();
        tasks.add(new Task("8888", "comment", "06:30", "07:30"));
        boolean result = Util.isSeparatedTime(tasks, new Task("7777", "comment", "06:30", "06:30"));
        boolean expectedResult = false;
        assertEquals(expectedResult, result);
    }
    
}
