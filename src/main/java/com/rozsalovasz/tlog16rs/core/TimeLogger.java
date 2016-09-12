package com.rozsalovasz.tlog16rs.core;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeLogger {

    private List<WorkMonth> months = new ArrayList();

    /**
     * This method adds a new month to the TimeLogger, 
     * if it is in the same year, as the earlier ones
     * @param workMonth the month to add
     * @throws NotSameYearException, if it is not in the same year like the earliers
     */
    public void addMonth(WorkMonth workMonth) throws NotSameYearException {
        if(isSameYear(workMonth))
        months.add(workMonth);
        else throw new NotSameYearException("You should add new month from the same year.");
    }

    /**
     * This method calculates the first month of the TimeLogger
     * @return with the first month
     * @throws NoMonthsException, if the months list is empty
     */
    public WorkMonth Min() throws NoMonthsException {
        if(!months.isEmpty()){
        WorkMonth min = months.get(0);
        for (WorkMonth workMonth : months) {
            if (min.getDays().get(0).getActualDay().getMonthValue() > workMonth.getDays().get(0).getActualDay().getMonthValue()) {
                min = workMonth;
            }
        }
        return min;
        }
        else throw new NoMonthsException("There are no months yet.");
    }

    /**
     * This method decides if the workMonth is in the same year as the months of the TimeLogger
     * @param workMonth the parameter about to decide
     * @return with true, if it is in the same year, false, if it is not in the same year
     */
    public boolean isSameYear(WorkMonth workMonth) {
        for (WorkMonth wm : months) {
            if ((workMonth.getDays().get(0).getActualDay().getYear() != wm.getDays().get(0).getActualDay().getYear()) && !months.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
