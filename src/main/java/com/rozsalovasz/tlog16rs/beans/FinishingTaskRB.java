package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FinishingTaskRB {
    int year;
    int month;
    int day;
    String taskId;
    String startTime;
    String endTime;

}
