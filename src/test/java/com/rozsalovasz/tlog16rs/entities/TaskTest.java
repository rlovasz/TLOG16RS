/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs.entities;

import java.time.LocalTime;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import java.time.format.DateTimeFormatter;
import org.junit.Test;
import static org.junit.Assert.*;

public class TaskTest {
    
    private Task getTaskWithoutComment() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("4868", null, "10:45", "11:30");
    }

    private Task getNotQuarterHourTask() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("1485", "This is a comment", 7, 35, 8, 45);
    }

    private Task getTaskWithMissingId() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task(null, "comment", 7, 30, 8, 45);
    }

    private Task getTaskWithNotExpectedTimeOrder() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("1485", "This is a comment", 8, 45, 7, 30);
    }

    private Task getTaskWithEmptyTimeField() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("LT-4894", "", "", "08:45");
    }

    private Task getNormalTask() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("LT-4894", "comment", "07:30", "08:45");
    }

    private Task getInvalidLTTask() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("LT-48954", "comment", "07:30", "08:45");
    }

    private Task getInvalidRedmineTask() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        return new Task("44894", "comment", "07:30", "08:45");
    }

    private Task getTaskWithOnlyTaskId() throws InvalidTaskIdException, NoTaskIdException {
        return new Task("1111");
    }

    @Test(expected = NotExpectedTimeOrderException.class)
    public void testTaskWithNegativeDuration() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getTaskWithNotExpectedTimeOrder();
    }

    @Test(expected = EmptyTimeFieldException.class)
    public void testGetMinPerTaskWithEmptyTimeField() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getTaskWithOnlyTaskId().getMinPerTask();
    }

    @Test
    public void testGetMinPerTaskNormal() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        long expResult = 75;
        long result = getNormalTask().getMinPerTask();
        assertEquals(expResult, result);
    }

    @Test(expected = NoTaskIdException.class)
    public void testTaskWithNoId() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getTaskWithMissingId();
    }

    @Test
    public void testGetCommentNoComment() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        String expResult = "";
        String result = getTaskWithoutComment().getComment();
        assertEquals(expResult, result);
    }

    @Test(expected = InvalidTaskIdException.class)
    public void testTaskWithInvalidRedmineId() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getInvalidRedmineTask();
    }

    @Test(expected = InvalidTaskIdException.class)
    public void testTaskWithInvalidLTId() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getInvalidLTTask();
    }

    @Test
    public void testCreateNormalTask() throws EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, NotExpectedTimeOrderException {
        Task normalTask = getNormalTask();
        assertEquals("LT-4894", normalTask.getTaskId());
        assertEquals("comment", normalTask.getComment());
        assertEquals(LocalTime.parse("07:30", DateTimeFormatter.ISO_TIME), normalTask.getStartTime());
        assertEquals(LocalTime.parse("08:45", DateTimeFormatter.ISO_TIME), normalTask.getEndTime());
    }

    @Test(expected = EmptyTimeFieldException.class)
    public void testCreateTaskWithEmptyTimeField() throws EmptyTimeFieldException, InvalidTaskIdException, NoTaskIdException, NotExpectedTimeOrderException {
        getTaskWithEmptyTimeField();
    }

    @Test
    public void testCreateTaskWithNotMultipleQuarterHour() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        Task task = getNotQuarterHourTask();
        assertEquals(LocalTime.of(8, 50), task.getEndTime());
        assertEquals(LocalTime.of(7, 35), task.getStartTime());
    }

    @Test
    public void testSetStartTimeToNotMultipleQuarterHour() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        Task task = getNormalTask();
        task.setStartTime(LocalTime.of(7, 35));
        assertEquals(LocalTime.of(8, 50), task.getEndTime());
        task = getNormalTask();
        task.setStartTime("07:35");
        assertEquals(LocalTime.of(8, 50), task.getEndTime());
        task = getNormalTask();
        task.setStartTime(7, 35);
        assertEquals(LocalTime.of(8, 50), task.getEndTime());
    }

    @Test(expected = NotExpectedTimeOrderException.class)
    public void testSetStartTimeToBeLaterThanEndTime() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getNormalTask().setStartTime("09:00");
    }

    @Test
    public void testSetStartTimeNormal() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        Task normalTask = getNormalTask();
        normalTask.setStartTime("07:00");
        assertEquals(LocalTime.of(7,0), normalTask.getStartTime());
    }

    @Test
    public void testSetEndTimeToNotMultipleQuarterHour() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        Task task = getNormalTask();
        task.setEndTime(LocalTime.of(9, 5));
        assertEquals(LocalTime.of(9, 0), task.getEndTime());
        task = getNormalTask();
        task.setEndTime("09:05");
        assertEquals(LocalTime.of(9, 0), task.getEndTime());
        task = getNormalTask();
        task.setEndTime(9, 5);
        assertEquals(LocalTime.of(9, 0), task.getEndTime());
    }

    @Test(expected = NotExpectedTimeOrderException.class)
    public void testSetEndTimeToBeEarlierThanStartTime() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getNormalTask().setEndTime("07:00");
    }
    
    @Test
    public void testSetEndTimeNormal() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        Task normalTask = getNormalTask();
        normalTask.setEndTime("09:00");
        assertEquals(LocalTime.of(9,0), normalTask.getEndTime());
    }

    @Test(expected = NoTaskIdException.class)
    public void testSetTaskIdToNull() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getNormalTask().setTaskId(null);
    }

    @Test(expected = InvalidTaskIdException.class)
    public void testSetTaskIdToInvalid() throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        getNormalTask().setTaskId("gsgsg");
    }
    
}