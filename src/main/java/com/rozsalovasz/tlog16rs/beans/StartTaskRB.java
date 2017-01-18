package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartTaskRB {
    private int year;
    private int month;
    private int day;
    private String taskId;
    private String startTime;
    private String comment;
    
}
