package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModifyTaskRB {
    private int year;
    private int month;
    private int day;
    private String taskId;
    private String startTime;
    private String newTaskId;
    private String newComment;
    private String newStartTime;
    private String newEndTime;
}
