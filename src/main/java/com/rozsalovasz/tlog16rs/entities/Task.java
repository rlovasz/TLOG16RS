package com.rozsalovasz.tlog16rs.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rozsalovasz.tlog16rs.Util;
import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * With the instantiation of this class we can create tasks. We can set a tasks
 * Id, the time of its start, the time of its end, we can add a comment to
 * detail. We can check if a task Id is valid and we can ask for the duration of
 * the task.
 *
 * @author rlovasz
 */
@Getter
@Entity
public class Task {

    @Id
    @GeneratedValue
    int id;

    private String taskId;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime startTime;
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime endTime;
    @Setter
    private String comment = "";
    private long minPerTask;

    /**
     * @param taskId This is the Id of the task. Redmine project: 4 digits, LT
     * project: LT-(4 digits)
     * @param comment In this parameter you can add some detail about what did
     * you do exactly.
     * @param startHour, the hour part of the beginning time
     * @param startMin, the mint part of the beginning time
     * @param endHour, the hour part of the finishing time
     * @param endMin , the min part of the finishing time
     * @throws com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws InvalidTaskIdException
     * @throws EmptyTimeFieldException
     */
    public Task(String taskId, String comment, int startHour, int startMin, int endHour, int endMin) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        LocalTime currentStartTime = LocalDateTime.ofInstant(Instant.from(LocalTime.of(startHour, startMin)), ZoneId.of("GMT+1")).toLocalTime();
        LocalTime currentEndTime = LocalDateTime.ofInstant(Instant.from(LocalTime.of(endHour, endMin)), ZoneId.of("GMT+1")).toLocalTime();
        this.minPerTask = 0;
        this.comment = comment;
        setValidValues(taskId, currentStartTime, currentEndTime);
    }

    /**
     *
     * @param taskId This is the Id of the task. Redmine project: 4 digits, LT
     * project: LT-(4 digits)
     * @throws InvalidTaskIdException
     * @throws com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException
     */
    public Task(String taskId) throws InvalidTaskIdException, NoTaskIdException {
        this.minPerTask = 0;
        if (!isValidTaskID(taskId)) {
            throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
        } else {
            this.taskId = taskId;
        }
    }

    /**
     *
     * @param taskId This is the Id of the task. Redmine project: 4 digits, LT
     * project: LT-(4 digits)
     * @param comment In this parameter you can add some detail about what did
     * you do exactly.
     * @param startTimeString the beginning time of task with string in format
     * HH:MM
     * @param endTimeString the finishing time of task with string in format
     * HH:MM
     * @throws com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws InvalidTaskIdException
     * @throws EmptyTimeFieldException
     * @throws java.text.ParseException
     */
    public Task(String taskId, String comment, String startTimeString, String endTimeString) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, ParseException {
        this.minPerTask = 0;
        LocalTime currentStartTime;
        LocalTime currentEndTime;
        if ("".equals(startTimeString)) {
            currentStartTime = null;
        } else {
            currentStartTime = Util.parseStringTime(startTimeString);
        }
        if ("".equals(endTimeString)) {
            currentEndTime = null;
        } else {
            currentEndTime = Util.parseStringTime(endTimeString);
        }
        this.comment = comment;
        setValidValues(taskId, currentStartTime, currentEndTime);
    }

    /**
     *
     * @param hour the value of hour with integer
     * @param min the value of minutes with integer
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     */
    public void setStartTime(int hour, int min) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        setStartTime(LocalDateTime.ofInstant(Instant.from(LocalTime.of(hour, min)), ZoneId.of("GMT+1")).toLocalTime());
    }

    /**
     *
     * @param hour the value of hour with integer
     * @param min the value of minutes with integer
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     */
    public void setEndTime(int hour, int min) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        setEndTime(LocalDateTime.ofInstant(Instant.from(LocalTime.of(hour, min)), ZoneId.of("GMT+1")).toLocalTime());
    }

    /**
     *
     * @param time The String value of time in format HH:MM
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     * @throws java.text.ParseException
     */
    public void setStartTime(String time) throws EmptyTimeFieldException, NotExpectedTimeOrderException, ParseException {
        setStartTime(Util.parseStringTime(time));
    }

    /**
     *
     * @param time The String value of time in format HH:MM
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     * @throws java.text.ParseException
     */
    public void setEndTime(String time) throws EmptyTimeFieldException, NotExpectedTimeOrderException, ParseException {
        setEndTime(Util.parseStringTime(time));
    }

    /**
     *
     * @param time The LocalTime value of time
     * @throws EmptyTimeFieldException
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     */
    public void setStartTime(LocalTime time) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        this.startTime = time;
        setToMultipleQuarterHours();
    }

    /**
     *
     * @param time The LocalTime value of time
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     */
    public void setEndTime(LocalTime time) throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        this.endTime = time;
        setToMultipleQuarterHours();
    }

    /**
     * @param taskId The parameter to set
     * @throws InvalidTaskIdException
     * @throws com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException
     */
    public void setTaskId(String taskId) throws InvalidTaskIdException, NoTaskIdException {
        if (!isValidTaskID(taskId)) {
            throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
        } else {
            this.taskId = taskId;
        }
    }

    /**
     * @return with the value of comment, but if it is not set, it returns with
     * an empty String
     */
    public String getComment() {
        if (comment == null) {
            comment = "";
        }

        return comment;
    }

    /**
     * This method is a getter for the minPerTask field.
     *
     * @return with the time interval between startTime and endTime in minutes
     * @throws EmptyTimeFieldException
     */
    public long getMinPerTask() throws EmptyTimeFieldException {
        if (startTime == null || endTime == null) {
            throw new EmptyTimeFieldException("You leaved out a time argument, you should set it.");
        } else {
            this.minPerTask = Duration.between(startTime, endTime).toMinutes();
            return minPerTask;
        }
    }

    /**
     * This method checks if the Id of the task is a valid redmine task Id.
     *
     * @return true, if it is valid, false if it isn't valid.
     */
    private boolean isValidRedmineTaskId(String taskId) {
        return taskId.matches("\\d{4}");
    }

    /**
     * This method checks if the Id of the task is a valid LT task Id.
     *
     * @return true, if it is valid, false if it isn't valid.
     */
    private boolean isValidLTTaskId(String taskId) {
        return taskId.matches("LT-\\d{4}");
    }

    /**
     * This method checks if the Id of the task is a valid task Id (redmine or
     * LT project task id).
     *
     * @return true, if it is valid, false if it isn't valid.
     * @throws NoTaskIdException
     */
    private boolean isValidTaskID(String taskId) throws NoTaskIdException {
        if (taskId == null || taskId.equals("")) {
            throw new NoTaskIdException("There is no task Id, please set a valid Id!");
        } else {
            return isValidLTTaskId(taskId) || isValidRedmineTaskId(taskId);
        }
    }

    /**
     * Checks if the duration of the task is multiple of quarter hour and rounds
     * it if not
     *
     * @param time only checks if this time is not null
     * @throws EmptyTimeFieldException
     * @throws NotExpectedTimeOrderException
     */
    private void setToMultipleQuarterHours() throws EmptyTimeFieldException, NotExpectedTimeOrderException {
        if (!Util.isMultipleQuarterHour(this.startTime, this.endTime)) {
            this.endTime = Util.roundToMultipleQuarterHour(this.startTime, this.endTime);
        }
    }

    /**
     * Checks if the given values are valid for the task
     *
     * @throws NoTaskIdException
     * @throws InvalidTaskIdException
     * @throws EmptyTimeFieldException
     * @throws NotExpectedTimeOrderException
     */
    private void setValidValues(String taskId, LocalTime startTime, LocalTime endTime) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        if (!Util.isMultipleQuarterHour(startTime, endTime)) {
            this.endTime = Util.roundToMultipleQuarterHour(startTime, endTime);
            this.startTime = startTime;
        } else {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        if (!isValidTaskID(taskId)) {
            throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
        } else {
            this.taskId = taskId;
        }
    }

    /**
     *
     * @return with the String representation of the Task type
     */
    @Override
    public String toString() {
        return "Task Id: " + taskId + ", Start time: " + startTime + ", End Time: " + endTime + ", Comment: " + comment;
    }

    private static class LocalTimeSerializer extends JsonSerializer<LocalTime> {

        @Override
        public void serialize(LocalTime t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            jg.writeString(t.toString());
        }
    }

}
