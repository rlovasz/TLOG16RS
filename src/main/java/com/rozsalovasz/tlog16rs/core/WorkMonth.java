package com.rozsalovasz.tlog16rs.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.time.YearMonth;
import java.util.*;
import lombok.Getter;

/**
 * With the instantiation of this class we can create work months. We can ask
 for the days in this date and we can change it. We can ask for the sum of
 the working minutes in this work date, and the extra minutes.
 *
 * @author precognox
 */
public class WorkMonth implements Comparable<WorkMonth>{

    @Getter
    private List<WorkDay> days = new ArrayList<>();
    @Getter
    @JsonSerialize(using = YearMonthSerializer.class)
    private YearMonth date;
    private long sumPerMonth = 0;
    private long requiredMinPerMonth = 0;

    /**
     * 
     * @param year This is the year of this date in YYYY format
     * @param month This is the date's value with a simple integer
     */
    public WorkMonth(int year, int month) {
        this.date = YearMonth.of(year, month);
    }
    
    /**
     * This method calculates all the minutes in this date while the employee worked
     *
     * @return with a positive value of worked minutes
     */
    public long getSumPerMonth(){
        if (sumPerMonth == 0) {
          return days.stream().mapToLong(WorkDay::getSumPerDay).sum();
        }
        return sumPerMonth;
    }

    /**
     * This method calculates all the extra worked minutes in this date
     *
     * @return with the signed value of extra minutes. If it is positive the
     * employee worked more, if it is negative the employee worked less, then
     * the required.
     */
    public long getExtraMinPerMonth(){
        if (requiredMinPerMonth == 0) {
            requiredMinPerMonth = getRequiredMinPerMonth();
        }
        return getSumPerMonth() - requiredMinPerMonth;
    }

    /**
     * This method calculates how many minutes should the employee work this date.
     *
     * @return with the integer value of minutes.
     */
    public long getRequiredMinPerMonth() {
        requiredMinPerMonth = 0;
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
     */
    public void addWorkDay(WorkDay workDay) {
        addWorkDay(workDay, false);
    }

    /**
     * This method adds a work day to this date, if the work day is a weekday.
     * But if it is on weekend we have to enable to work on weekend.
     *
     * @param workDay This is a WorkDay parameter, which will be added.
     * @param isWeekendEnabled This is a boolean parameter, if it is false, we
     * cannot work on weekend, but if it is true, we can add a day of weekend to
     * this date.
     */
    public void addWorkDay(WorkDay workDay, boolean isWeekendEnabled) {
        if (isNewDate(workDay) && (isWeekendEnabled || workDay.isWeekday()) && isSameMonth(workDay)) {
            days.add(workDay);
            sumPerMonth = 0;
            requiredMinPerMonth = 0;
        } else if (!isNewDate(workDay)) {
            throw new NotNewDateException("You have already added this day. You should choose an other day!");
        } else if (!isSameMonth(workDay)) {
            throw new NotTheSameMonthException("You have changed the month, so you should add this to an other month!");
        } else {
            throw new WeekendNotEnabledException("You cannot add this day, because it is on weekend and it is not enabled.");
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
            if (!days.isEmpty() && wd.getActualDay().equals(workDay.getActualDay())) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method decides, if the parameter has the same date value, like the days
     *
     * @param workDay parameter about to decide
     * @return true if it is the same date, false, if it is not
     */
    protected boolean isSameMonth(WorkDay workDay) {
        
            if ((hasDifferentMonthValue(workDay) || hasDifferentYearValue(workDay)) ) {
                return false;
            }
        
        return true;
    }

    /**
     * Decides if the WorkDay parameter is in a different year as this month
     * @param workDay
     * @return true, if they are in the same year, false, if they are not
     */
    private boolean hasDifferentYearValue(WorkDay workDay) {
        return workDay.getActualDay().getYear() != date.getYear();
    }

    /**
     * Decides if the WorkDay parameter is in a different month
     * @param workDay
     * @return true, if it is in this month, false, if it is not
     */
    private boolean hasDifferentMonthValue(WorkDay workDay) {
        return workDay.getActualDay().getMonthValue() != date.getMonthValue();
    }

    @Override
    public int compareTo(WorkMonth otherMonth) {
        return this.date.compareTo(otherMonth.date);
    }

    private static class YearMonthSerializer extends JsonSerializer<YearMonth>{

        public YearMonthSerializer() {
        }

        @Override
        public void serialize(YearMonth t, JsonGenerator jg, SerializerProvider sp) throws IOException, JsonProcessingException {
            jg.writeString(t.toString());
        }
    }

}
