package com.rozsalovasz.tlog16rs.core;

import java.time.Duration;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * With the instantiation of this class we can create tasks. We can set a tasks
 * Id, the time of its start, the time of its end, we can add a comment to
 * detail. We can check if a task Id is valid and we can ask for the duration of
 * the task.
 *
 * @author precognox
 */
@Getter
@NoArgsConstructor
public class Task {

    private String taskId;
    @Setter
    private String comment = "";
    @Setter
    private LocalTime startTime;
    @Setter
    private LocalTime endTime;
    private long minPerTask;

    
    /**
     * @param taskId This is the Id of the task. Redmine project: 4 digits, LT
     * project: LT-(4 digits)
     * @param comment In this parameter you can add some detail about what did
     * you do exactly.
     * @param startTime You can set the time when you started the task:
     * LocalTime.of(hh,mm)
     * @param endTime You can set the time when you finished the task:
     * LocalTime.of(hh,mm)
     * @throws com.rozsalovasz.tlog16rs.core.InvalidTaskIdException,
     * if the set task Id is not valid
     * @throws com.rozsalovasz.tlog16rs.core.NoTaskIdException, 
     * if there is no task Id set
     */
    public Task(String taskId, String comment, LocalTime startTime, LocalTime endTime) throws InvalidTaskIdException, NoTaskIdException{
        
        this.taskId = taskId;
        this.comment = comment;
        this.startTime = startTime;
        this.endTime = endTime;
        if(!isValidTaskID())
        {
        throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
        }

    }
    
    /**
     * @param taskId the method sets this parameter as taskId
     * @throws InvalidTaskIdException, if the set task Id is not valid
     * @throws NoTaskIdException, if there is no task Id set
     */
    public void setTaskId(String taskId) throws InvalidTaskIdException, NoTaskIdException {
        this.taskId = taskId;
        if(!isValidTaskID())
        {
        throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
        }
         
    }

    /**
     * @return with the value of task Id, if it is set
     * @throws NoTaskIdException , if task Id is not set
     */
    public String getTaskId() throws NoTaskIdException {
        if (taskId == null) {
            throw new NoTaskIdException("There is no task Id, please set a valid Id!");
        } else {
            return taskId;
        }
    }

    /**
     * @return with the value of comment, but if it is not set, 
     * it returns with an empty String
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
     * @throws com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException, 
     * if the task begins after it ends
     * @throws com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException,
     * if any of the LocalTime  parameter is empty
     */
    public long getMinPerTask() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (startTime == null || endTime == null || (startTime == null && endTime == null)) {
            throw new EmptyTimeFieldException("You leaved out a time argument, you should set it.");
        } else if (startTime.isBefore(endTime)) {
            return Duration.between(startTime, endTime).toMinutes();
        } else {
            throw new NotExpectedTimeOrderException("Something went wrong. You should begin"
                    + " your task before you finish it.");
        }

    }

    /**
     * This method checks if the Id of the task is a valid redmine task Id.
     *
     * @return true, if it is valid, false if it isn't valid.
     * @throws com.rozsalovasz.tlog16rs.core.NoTaskIdException,
     * if task Id is not set
     */
    protected boolean isValidRedmineTaskId() throws NoTaskIdException {
        if (taskId == null) {
            throw new NoTaskIdException("There is no task Id, please set a valid Id!");
        } else {
            return taskId.matches("\\d{4}");
        }
    }

    /**
     * This method checks if the Id of the task is a valid LT task Id.
     *
     * @return true, if it is valid, false if it isn't valid.
     * @throws com.rozsalovasz.tlog16rs.core.NoTaskIdException, 
     * if task Id is not set
     */
    protected boolean isValidLTTaskId() throws NoTaskIdException {
        if (taskId == null) {
            throw new NoTaskIdException("There is no task Id, please set a valid Id!");
        } else {
            return taskId.matches("LT-\\d{4}");
        }
    }

    /**
     * This method checks if the Id of the task is a valid task Id (redmine or
     * LT project task id).
     *
     * @return true, if it is valid, false if it isn't valid.
     * @throws com.rozsalovasz.tlog16rs.core.NoTaskIdException,
     * if task Id is not set
     */
    private boolean isValidTaskID() throws NoTaskIdException {

        return isValidLTTaskId() || isValidRedmineTaskId();
    }

    /**
     * This method checks if the minutes are the multiple of quarter hour
     *
     * @return true, if it is multiple, but false if it isn't.
     * @throws com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if the task begins after it ends
     * @throws com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException,
     * if any of the LocalTime  parameter is empty
     */
    protected boolean isMultipleQuarterHour() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        return getMinPerTask() % 15 == 0;

    }

}
