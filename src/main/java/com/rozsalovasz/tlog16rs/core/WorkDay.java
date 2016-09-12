package com.rozsalovasz.tlog16rs.core;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * With the instantiation of this class we can create work days. We can set the
 * date of the work day, and the required minutes we should work today. We can
 * check if the work day is weekday, and we can add tasks for the day. We can
 * ask for the sum of the working minutes on this work day, and the extra
 * minutes.
 *
 * @author precognox
 */
@Getter
public class WorkDay {

    private static final List<DayOfWeek> WEEKDAYS = new ArrayList<>(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
    private static final int DEFAULT_REQUIRED_MIN_PER_DAY = 450;
    @Setter
    private List<Task> tasks = new ArrayList<>();
    private long requiredMinPerDay;
    private LocalDate actualDay;
    private long extraMinPerDay;
    private long sumPerDay = 0;

    /**
     * @param requiredMinPerDay In this parameter you can set the minutes you
     * should work today.
     * @param actualDay In this parameter you can set the date, it will be the
     * actual day.
     * @throws com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException,
     * if requiredMinPerDay has a negative value
     * @throws com.rozsalovasz.tlog16rs.core.FutureWorkException,
     * if actualDay's date is in the future
     */
    public WorkDay(long requiredMinPerDay, LocalDate actualDay) throws NegativeMinutesOfWorkException, FutureWorkException {
        if (requiredMinPerDay <= 0) {
            throw new NegativeMinutesOfWorkException("You set a negative value for required minutes, you should set a non-negative value!");
        }
        if (actualDay.isAfter(LocalDate.now())) {
            throw new FutureWorkException("You cannot work later than today, you should set an other day!");
        }
        this.requiredMinPerDay = requiredMinPerDay;
        this.actualDay = actualDay;
    }

    /**
     * The default actual day will be today (server time).
     *
     * @param requiredMinPerDay In this parameter you can set the minutes you
     * should work today.
     * @throws com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException,
     * if requiredMinPerDay has a negative value
     * @throws com.rozsalovasz.tlog16rs.core.FutureWorkException,
     * if actualDay's date is in the future
     */
    public WorkDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException, FutureWorkException {
        this(requiredMinPerDay, LocalDate.now());
    }

    /**
     * The default required minutes will be 450 min=7.5 h
     *
     * @param actualDay In this parameter you can set the date, it will be the
     * actual day.
     * @throws com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException,
     * if requiredMinPerDay has a negative value
     * @throws com.rozsalovasz.tlog16rs.core.FutureWorkException,
     * if actualDay's date is in the future
     */
    public WorkDay(LocalDate actualDay) throws NegativeMinutesOfWorkException, FutureWorkException {
        this(DEFAULT_REQUIRED_MIN_PER_DAY, actualDay);
    }

    /**
     * The default actual day will be today (server time), the default required
     * minutes will be 450 min = 7.5 h
     * @throws com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException,
     * if requiredMinPerDay has a negative value
     * @throws com.rozsalovasz.tlog16rs.core.FutureWorkException,
     * if actualDay's date is in the future
     */
    public WorkDay() throws NegativeMinutesOfWorkException, FutureWorkException {
        this(DEFAULT_REQUIRED_MIN_PER_DAY, LocalDate.now());
    }

    /**
     * We can set the amount of the minutes the employee should work this day.
     *
     * @param requiredMinPerDay the value which will be set
     * @throws NegativeMinutesOfWorkException, if we try to set a negative value
     */
    public void setRequiredMinPerDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException {
        if (requiredMinPerDay <= 0) {
            throw new NegativeMinutesOfWorkException("You set a negative value for required minutes, you should set a non-negative value!");
        }
        this.requiredMinPerDay = requiredMinPerDay;
    }

    /**
     * We can set the date of the actual day.
     *
     * @param actualDay the value which will be set
     * @throws FutureWorkException, if we try to set a date after today
     */
    public void setActualDay(LocalDate actualDay) throws FutureWorkException {
        if (actualDay.isAfter(LocalDate.now())) {
            throw new FutureWorkException("You cannot work later than today, you should set an other day!");
        }
        this.actualDay = actualDay;
    }

    /**
     * This method calculates the difference between the minutes while the
     * employee worked and the minutes while the employee should have worked
     *
     * @return with the signed value of the extra minutes on this work day. If
     * it is positive the employee worked more, if it is negative the employee
     * worked less, then the required.
     * @throws com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if a task of this day begins later then it ends
     * @throws com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException, 
     * if a task of this day has empty time field
     */
    public long getExtraMinPerDay() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        return getSumPerDay() - requiredMinPerDay;
    }

    /**
     * This methods calculates the sum of the minutes of the tasks of this work
     * day.
     *
     * @return with the minutes while the employee worked on this work day
     * @throws com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if a task of this day begins later then it ends
     * @throws com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException,
     * if a task of this day has empty time field
     */
    public long getSumPerDay() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (sumPerDay == 0) {
            for (Task task : tasks) {
                sumPerDay += task.getMinPerTask();
            }
        }
        return sumPerDay;
    }

    /**
     * This method adds a new task to the List named tasks, after it checks if
     * the minutes of the task are the multiple of a quarter hour. If it would
     * be false, this method throws
     *
     * @param task It is a Task type parameter, which will be added
     * @throws com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException,
     * if the task, which we are trying to add has a time interval which is not 
     * the multiple of the quarter hour
     * @throws com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if a task of this day begins later then it ends
     * @throws com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException,
     * if a task of this day has empty time field
     * @throws com.rozsalovasz.tlog16rs.core.NotSeparatedTaskTimesException,
     * if the task, which we are trying to add has a common time interval with 
     * one of the existing task
     */
    public void addTask(Task task) throws NotMultipleQuarterHourException, NotExpectedTimeOrderException, EmptyTimeFieldException, NotSeparatedTaskTimesException {
        if (task.isMultipleQuarterHour() && isSeparatedTime(task)) {
            tasks.add(task);
            sumPerDay = 0;
        } else if (!isSeparatedTime(task)) {
            throw new NotSeparatedTaskTimesException("You should separate the time intervals of your tasks!");
        } else {
            throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
                    + "Please reconsider the time interval");
        }
    }

    /**
     * This method decides if this work day is a weekday or notexpResult
     *
     * @return true if it is a weekday, false if it is on weekend
     */
    protected boolean isWeekday() {
        return WEEKDAYS.contains(DayOfWeek.from(actualDay));
    }

    /**
     * This method decides, if the task parameter has common time interval with
     * one of the existing tasks
     *
     * @param task the parameter to check
     * @return true, if there is no common time interval, false, if there is a
     * common time interval
     */
    protected boolean isSeparatedTime(Task task) {
        for (Task t : tasks) {
            if (t.getStartTime().isBefore(task.getStartTime())) {
                if (task.getStartTime().isBefore(t.getEndTime())) {
                    return false;
                }
            } else if (t.getStartTime().isAfter(task.getStartTime())) {
                if (t.getStartTime().isBefore(task.getEndTime())) {
                    return false;
                }
            } else if (t.getEndTime().equals(task.getEndTime())) {
                return false;
            }
        }
        return true;
    }

}
