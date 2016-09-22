package com.rozsalovasz.tlog16rs.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

public class TimeLogger {

    @Getter
    private List<WorkMonth> months = new ArrayList();

    /**
     * This method adds a new month to the TimeLogger, if it is in the same
     * year, as the earlier ones
     *
     * @param workMonth the month to add
     */
    public void addMonth(WorkMonth workMonth) {
        if (isNewMonth(workMonth)) {
            months.add(workMonth);
        } else {
            throw new NotNewMonthException("This month is already exists.");
        }

    }

    /**
     * This method decides if the work month is in the list of the months
     * already
     *
     * @param workMonth, the parameter about to decide
     * @return true, if it is new, false, if it is already exists
     */
    private boolean isNewMonth(WorkMonth workMonth) {
        for (WorkMonth wm : months) {
            if ((wm.getDate().equals(workMonth.getDate())) && !months.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method calculates the first month of the TimeLogger
     *
     * @return with the first month
     */
    public WorkMonth getFirstMonthOfTimeLogger(){
        if (!months.isEmpty()) {
            return Collections.min(months);
        }

        throw new NoMonthsException("There are no months yet.");
    }

}
