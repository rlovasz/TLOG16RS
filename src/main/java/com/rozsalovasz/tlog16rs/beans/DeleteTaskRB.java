package com.rozsalovasz.tlog16rs.beans;

import lombok.Getter;

@Getter
public class DeleteTaskRB {
    int year;
    int month;
    int day;
    String taskId;
    String startTime;
}
