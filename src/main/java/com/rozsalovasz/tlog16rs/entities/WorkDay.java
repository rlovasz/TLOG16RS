package com.rozsalovasz.tlog16rs.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rozsalovasz.tlog16rs.Util;
import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.FutureWorkException;
import com.rozsalovasz.tlog16rs.exceptions.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;

/**
 * With the instantiation of this class we can create work days. We can set the
 * date of the work day, and the required minutes we should work today. We can
 * check if the work day is weekday, and we can add tasks for the day. We can
 * ask for the sum of the working minutes on this work day, and the extra
 * minutes.
 *
 * @author rlovasz
 */
@Getter
@Entity
public class WorkDay {

    @Id
    @GeneratedValue
    int id;

    private static final int DEFAULT_REQUIRED_MIN_PER_DAY = (int) (7.5 * 60);
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Task> tasks = new ArrayList<>();
    private long requiredMinPerDay;
    private long extraMinPerDay;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate actualDay;
    private long sumPerDay;

    /**
     * @param requiredMinPerDay In this parameter you can set the minutes you
     * should work today.
     * @param year
     * @param month
     * @param day
     * @throws com.rozsalovasz.tlog16rs.exceptions.FutureWorkException
     * @throws NegativeMinutesOfWorkException
     */
    public WorkDay(long requiredMinPerDay, int year, int month, int day) throws FutureWorkException, NegativeMinutesOfWorkException {
        LocalDate currentDay = LocalDate.of(year, month, day);
        if (requiredMinPerDay <= 0) {
            throw new NegativeMinutesOfWorkException("You set a negative value for required minutes, you should set a non-negative value!");
        }
        if (currentDay.isAfter(LocalDate.now())) {
            throw new FutureWorkException("You cannot work later than today, you should set an other day!");
        }
        this.requiredMinPerDay = requiredMinPerDay;
        this.extraMinPerDay = -requiredMinPerDay;
        this.sumPerDay = 0;
        this.actualDay = currentDay;
    }

    /**
     * The default actual day will be today (server time).
     *
     * @param requiredMinPerDay In this parameter you can set the minutes you
     * should work today.
     * @throws com.rozsalovasz.tlog16rs.exceptions.FutureWorkException
     * @throws NegativeMinutesOfWorkException
     */
    public WorkDay(long requiredMinPerDay) throws NegativeMinutesOfWorkException, FutureWorkException {
        this(requiredMinPerDay, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
    }

    /**
     * The default required minutes will be 450 min=7.5 h
     *
     * @param year, the year value of the date in YYYY format
     * @param month, the month value of the date with simple integer value
     * @param day, the day value of the date with simple integer value
     * @throws com.rozsalovasz.tlog16rs.exceptions.FutureWorkException
     * @throws NegativeMinutesOfWorkException
     */
    public WorkDay(int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException {
        this(DEFAULT_REQUIRED_MIN_PER_DAY, year, month, day);
    }

    /**
     * The default actual day will be today (server time), the default required
     * minutes will be 450 min = 7.5 h
     *
     * @throws com.rozsalovasz.tlog16rs.exceptions.FutureWorkException
     * @throws NegativeMinutesOfWorkException
     */
    public WorkDay() throws NegativeMinutesOfWorkException, FutureWorkException {
        this(DEFAULT_REQUIRED_MIN_PER_DAY, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
    }

    /**
     * We can set the amount of the minutes the employee should work this day.
     *
     * @param requiredMinPerDay the value which will be set
     * @throws NegativeMinutesOfWorkException
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
     * @param year, the year value of the date in YYYY format
     * @param month, the month value of the date with simple integer value
     * @param day, the day value of the date with simple integer value
     * @throws com.rozsalovasz.tlog16rs.exceptions.FutureWorkException
     */
    public void setActualDay(int year, int month, int day) throws FutureWorkException {
        LocalDate currentDay = LocalDate.of(year, month, day);
        if (currentDay.isAfter(LocalDate.now())) {
            throw new FutureWorkException("You cannot work later than today, you should set an other day!");
        }
        this.actualDay = currentDay;
    }

    /**
     * This method calculates the difference between the minutes while the
     * employee worked and the minutes while the employee should have worked
     *
     * @return with the signed value of the extra minutes on this work day. If
     * it is positive the employee worked more, if it is negative the employee
     * worked less, then the required.
     */
    public long getExtraMinPerDay() {
        this.extraMinPerDay = getSumPerDay() - requiredMinPerDay;
        return this.extraMinPerDay;
    }

    /**
     * This methods calculates the sum of the minutes of the tasks of this work
     * day.
     *
     * @return with the minutes while the employee worked on this work day
     * @throws EmptyTimeFieldException
     */
    public long getSumPerDay() throws EmptyTimeFieldException {
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
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException
     */
    public void addTask(Task task) throws NotSeparatedTaskTimesException {
        if (Util.isSeparatedTime(tasks, task)) {
            tasks.add(task);
            sumPerDay = 0;
        } else {
            throw new NotSeparatedTaskTimesException("You should separate the time intervals of your tasks!");
        }
    }

    /**
     * Removes the given task from the list of tasks
     * @param task
     */
    public void removeTask(Task task) {
        tasks.remove(task);
    }

    private static class LocalDateSerializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate t, JsonGenerator jg, SerializerProvider sp) throws IOException {
            jg.writeString(t.toString());
        }
    }

}
