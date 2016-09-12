package com.rozsalovasz.tlog16rs.core;

import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * With the instantiation of this class we can create work months. We can ask
 * for the days in this month and we can change it. We can ask for the sum of
 * the working minutes in this work month, and the extra minutes.
 *
 * @author precognox
 */
@NoArgsConstructor
public class WorkMonth {

    @Setter
    @Getter
    private List<WorkDay> days = new ArrayList<>();
    private long sumPerMonth = 0;
    private long requiredMinPerMonth = 0;

    /**
     * This method calculates all the minutes in this month while the employee
     * worked
     *
     * @return with a positive value of worked minutes
     * @throws
     * com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if one of the tasks begins after it ends
     * @throws
     * com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException, if
     * one of the tasks has empty time field
     */
    public long getSumPerMonth() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (sumPerMonth == 0) {
            for (WorkDay workDay : days) {
                sumPerMonth += workDay.getSumPerDay();
            }
        }
        return sumPerMonth;
    }

    /**
     * This method calculates all the extra worked minutes in this month
     *
     * @return with the signed value of extra minutes. If it is positive the
     * employee worked more, if it is negative the employee worked less, then
     * the required.
     * @throws
     * com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException,
     * if one of the tasks begins after it ends
     * @throws
     * com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException, if
     * one of the tasks has empty time field
     */
    public long getExtraMinPerMonth() throws NotExpectedTimeOrderException, EmptyTimeFieldException {
        if (requiredMinPerMonth == 0) {
            requiredMinPerMonth = getRequiredMinPerMonth();
        }
        return getSumPerMonth() - requiredMinPerMonth;
    }

    /**
     * This method calculates how many minutes should the employee work this
     * month.
     *
     * @return with the integer value of minutes.
     */
    public long getRequiredMinPerMonth() {
        days.stream().forEach((wd) -> {
            requiredMinPerMonth += wd.getRequiredMinPerDay();
        });
        return requiredMinPerMonth;
    }

    /**
     * This method is an overloaded method of addWorkDay(WorkDay,boolean) with
     * the default false value: addWorkDay(WorkDay,false)
     *
     * @param workDay This is a WorkDay parameter, which will be added.
     * @throws
     * com.rozsalovasz.tlog16rs.core.WeekendIsNotEnabledException,
     * if we try to add a day of a weekend while it is not enabled
     * @throws com.rozsalovasz.tlog16rs.core.NotNewDateException,
     * if we try to add a date, which is already exists
     * @throws com.rozsalovasz.tlog16rs.core.NotTheSameMonthException,
     * if we try to add a day from an other month
     */
    public void addWorkDay(WorkDay workDay) throws WeekendIsNotEnabledException, NotNewDateException, NotTheSameMonthException {
        addWorkDay(workDay, false);
    }

    /**
     * This method adds a work day to this month, if the work day is a weekday.
     * But if it is on weekend we have to enable to work on weekend.
     *
     * @param workDay This is a WorkDay parameter, which will be added.
     * @param isWeekendEnabled This is a boolean parameter, if it is false, we
     * cannot work on weekend, but if it is true, we can add a day of weekend to
     * this month.
     * @throws
     * com.rozsalovasz.tlog16rs.core.WeekendIsNotEnabledException,
     * if we try to add a weekend and it is enabled
     * @throws com.rozsalovasz.tlog16rs.core.NotNewDateException,
     * the day is already exists, what we are trying to add
     * @throws com.rozsalovasz.tlog16rs.core.NotTheSameMonthException, 
     * if we try to add a day from an other month
     */
    public void addWorkDay(WorkDay workDay, boolean isWeekendEnabled) throws WeekendIsNotEnabledException, NotNewDateException, NotTheSameMonthException {
        if ((workDay.isWeekday() || isWeekendEnabled) && isNewDate(workDay) && isSameMonth(workDay)) {
            days.add(workDay);
            sumPerMonth = 0;
            requiredMinPerMonth = 0;
        } else if (!isNewDate(workDay)) {
            throw new NotNewDateException("You have already added this day. You should choose an other day!");
        } else if (!isSameMonth(workDay)) {
            throw new NotTheSameMonthException("You have changed the month, so you should add this to an other month!");
        } else {
            throw new WeekendIsNotEnabledException("You cannot add this day, because it is on weekend and it is not enabled.");
        }
    }

    /**
     * This method decides if the date of workDay already exist in the list of
     * days
     *
     * @param workDay the day we check
     * @return true, if it is a new date, false if it isn't new.
     */
    protected boolean isNewDate(WorkDay workDay) {
        for (WorkDay wd : days) {
            if (wd.getActualDay().equals(workDay.getActualDay())) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method decides, if the parameter has the same month value, like the
     * days
     *
     * @param workDay parameter about to decide
     * @return true if it is the same month, false, if it is not
     */
    public boolean isSameMonth(WorkDay workDay) {
        for (WorkDay wd : days) {
            if (wd.getActualDay().getMonthValue() != workDay.getActualDay().getMonthValue()) {
                return false;
            }
        }
        return true;
    }

}
