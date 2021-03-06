/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rozsalovasz.tlog16rs;

import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.entities.Task;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Util {

    /**
     * This method rounds the given duration to be multiple of quarter hour
     *
     * @param startTime the beginning of the time interval
     * @param endTime the end of the time interval
     * @return returns with the new endTime for the interval
     */
    public static LocalTime roundToMultipleQuarterHour(LocalTime startTime, LocalTime endTime) {
        long taskLengthInMinutes = Math.round((float) Duration.between(startTime, endTime).toMinutes() / 15) * 15;
        return startTime.plusMinutes(taskLengthInMinutes);
    }

    /**
     * This method checks if the minutes are the multiple of quarter hour
     *
     * @param startTime
     * @param endTime
     * @return true, if it is multiple, but false if it isn't.
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException
     * @throws EmptyTimeFieldException
     */
    public static boolean isMultipleQuarterHour(LocalTime startTime, LocalTime endTime) throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (startTime == null || endTime == null) {
            throw new EmptyTimeFieldException("You leaved out a time argument, you should set it.");
        } else if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
            return Duration.between(startTime, endTime).toMinutes() % 15 == 0;
        } else {
            throw new NotExpectedTimeOrderException("Something went wrong. You should begin"
                    + " your task before you finish it.");
        }

    }

    /**
     * This method decides, if the task parameter has common time interval with
     * one of the existing tasks
     *
     * @param tasks
     * @param task the parameter to check
     * @return true, if there is no common time interval, false, if there is a
     * common time interval
     */
    public static boolean isSeparatedTime(List<Task> tasks, Task task) {
        boolean isSeparated = true;
        for (Task t : tasks) {
            boolean existingBeginsEarlier = checkTheNotBeginsOrEndsTogetherCase(t, task);
            boolean newBeginsEarlier = checkTheNotBeginsOrEndsTogetherCase(task, t);
            boolean endsOrBeginsTogether = checkTheEndsOrBeginsTogetherCase(t, task);
            isSeparated = !(existingBeginsEarlier || newBeginsEarlier || endsOrBeginsTogether);
            if (isSeparated == false) {
                break;
            }
        }
        return isSeparated;
    }

    /**
     * Parse different types of time formats
     * @param time
     * @return with the time as LocalTime
     * @throws ParseException 
     */
    public static LocalTime parseStringTime(String time) throws ParseException {
        String[] formatStrings = {"HH:mm", "H:mm", "H:m", "HH:m", "HHmm", "Hmm"};
        for (String formatString : formatStrings) {
            try {
                return LocalTime.parse(time, DateTimeFormatter.ofPattern(formatString));
            } catch (DateTimeParseException e) {

            }
        }
        throw new ParseException(time, 0);
    }

    private static boolean checkTheNotBeginsOrEndsTogetherCase(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getStartTime()) && task2.getStartTime().isBefore(task1.getEndTime());
    }

    private static boolean checkTheEndsOrBeginsTogetherCase(Task existingTask, Task newTask) {
        return (existingTask.getEndTime().equals(newTask.getEndTime()) && (existingTask.getMinPerTask() != 0 && newTask.getMinPerTask() != 0))
                || existingTask.getStartTime().equals(newTask.getStartTime());
    }
}
