package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartTaskRB {
    int year;
    int month;
    int day;
    String taskId;
    String startTime;
    String comment;
    
}
