package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkDayRB {
    int year;
    int month;
    int day;
    double requiredHours;
 
}
