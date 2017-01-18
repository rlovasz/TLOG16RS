package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkDayRB {
    private int year;
    private int month;
    private int day;
    private double requiredHours;
 
}
