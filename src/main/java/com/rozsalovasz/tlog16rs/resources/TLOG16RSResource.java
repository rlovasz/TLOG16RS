package com.rozsalovasz.tlog16rs.resources;

import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.WorkDayRB;
import com.rozsalovasz.tlog16rs.beans.WorkMonthRB;
import com.rozsalovasz.tlog16rs.core.NotNewMonthException;
import com.rozsalovasz.tlog16rs.core.Task;
import com.rozsalovasz.tlog16rs.core.TimeLogger;
import com.rozsalovasz.tlog16rs.core.WorkDay;
import com.rozsalovasz.tlog16rs.core.WorkMonth;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/timelogger")
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    private TimeLogger timeLogger = new TimeLogger();


    /*@POST
    @Path("/workmonths/{year}/{month}")
    public WorkMonth addNewMonthPath(@PathParam("year") int year, @PathParam("month") int month) {
        WorkMonth workMonth = new WorkMonth(year, month);
        timeLogger.addMonth(workMonth);
        return workMonth;
    }*/
    @POST
    @Path("/workmonths")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WorkMonth addNewMonth(WorkMonthRB month) {
        try {
            WorkMonth workMonth = new WorkMonth(month.getYear(), month.getMonth());
            timeLogger.addMonth(workMonth);
            return workMonth;
        } catch (NotNewMonthException ex) {
            System.err.println(ex.getMessage());
            return new WorkMonth(1970, 1);
        }
    }

    @GET
    @Path("/workmonths")
    public List<WorkMonth> listWorkMonths() {
        try {
            return timeLogger.getMonths();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    @PUT
    @Path("/workmonths/deleteall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAllMonths() {
        try {
            while (timeLogger.getMonths().size() != 0) {
                timeLogger.getMonths().remove(0);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @GET
    @Path("/workmonths/{year}/{month}")
    public List<WorkDay> listSpecificMonth(@PathParam("year") int year, @PathParam("month") int month) {
        try {
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(year, month))) {
                    return workMonth.getDays();
                }
            }
            WorkMonth workMonth = new WorkMonth(year, month);
            timeLogger.addMonth(workMonth);
            return workMonth.getDays();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    @POST
    @Path("/workmonths/workdays")
    @Consumes(MediaType.APPLICATION_JSON)
    public WorkDay addNewDay(WorkDayRB day) {
        try {
            WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(workDay.getActualDay().getYear(), workDay.getActualDay().getMonthValue()))) {
                    workMonth.addWorkDay(workDay);
                    return workDay;
                }
            }
            WorkMonth workMonth = new WorkMonth(day.getYear(), day.getMonth());
            timeLogger.addMonth(workMonth);
            workMonth.addWorkDay(workDay);
            return workDay;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new WorkDay(1970, 1, 1);
        }

    }

    @GET
    @Path("/workmonths/{year}/{month}/{day}")
    public List<Task> listSpecificDay(@PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) {
        try {
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(year, month))) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().getDayOfMonth() == day) {
                            return workDay.getTasks();
                        }
                    }
                    WorkDay workDay = new WorkDay(year, month, day);
                    workMonth.addWorkDay(workDay);
                    return workDay.getTasks();
                }
            }
            WorkMonth workMonth = new WorkMonth(year, month);
            timeLogger.addMonth(workMonth);
            WorkDay workDay = new WorkDay(year, month, day);
            workMonth.addWorkDay(workDay);
            return workDay.getTasks();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    @POST
    @Path("/workmonths/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task startNewTask(StartTaskRB task) {
        try {
            Task startedTask = new Task(task.getTaskId());
            startedTask.setComment(task.getComment());
            startedTask.setStartTime(task.getStartTime());
            startedTask.setEndTime(task.getStartTime());
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(task.getYear(), task.getMonth()))) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                            workDay.addTask(startedTask);
                            return startedTask;
                        }
                    }
                    WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                    workMonth.addWorkDay(workDay);
                    workDay.addTask(startedTask);
                    return startedTask;
                }
            }
            WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
            timeLogger.addMonth(workMonth);
            WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
            workMonth.addWorkDay(workDay);
            workDay.addTask(startedTask);
            return startedTask;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Task("0000");
        }
    }

    @PUT
    @Path("/workmonths/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task finishStartedTask(FinishingTaskRB task) {
        try {
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(task.getYear(), task.getMonth()))) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                            for (Task startedTask : workDay.getTasks()) {
                                if (task.getTaskId().equals(startedTask.getTaskId())
                                        && Task.stringToLocalTime(task.getStartTime()).equals(startedTask.getStartTime())) {
                                    startedTask.setEndTime(task.getEndTime());
                                    return startedTask;
                                }
                            }
                            Task startedTask = new Task(task.getTaskId());
                            startedTask.setStartTime(task.getStartTime());
                            startedTask.setEndTime(task.getEndTime());
                            workDay.addTask(startedTask);
                            return startedTask;
                        }
                    }
                    WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                    workMonth.addWorkDay(workDay);
                    Task startedTask = new Task(task.getTaskId());
                    startedTask.setStartTime(task.getStartTime());
                    startedTask.setEndTime(task.getEndTime());
                    workDay.addTask(startedTask);
                    return startedTask;
                }
            }
            WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
            timeLogger.addMonth(workMonth);
            WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
            workMonth.addWorkDay(workDay);
            Task startedTask = new Task(task.getTaskId());
            startedTask.setStartTime(task.getStartTime());
            startedTask.setEndTime(task.getEndTime());
            workDay.addTask(startedTask);
            return startedTask;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new Task("0000");
        }
    }

    @PUT
    @Path("/workmonths/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Task modifyExistingTask(ModifyTaskRB task) {
        try {
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(task.getYear(), task.getMonth()))) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                            for (Task existingTask : workDay.getTasks()) {
                                if (task.getTaskId().equals(existingTask.getTaskId())
                                        && Task.stringToLocalTime(task.getStartTime()).equals(existingTask.getStartTime())) {
                                    existingTask.setTaskId(task.getNewTaskId());
                                    existingTask.setComment(task.getNewComment());
                                    existingTask.setStartTime(task.getNewStartTime());
                                    existingTask.setEndTime(task.getNewEndTime());
                                    return existingTask;
                                }
                            }
                            Task newTask = new Task(task.getNewTaskId());
                            newTask.setComment(task.getNewComment());
                            newTask.setStartTime(task.getNewStartTime());
                            newTask.setEndTime(task.getNewEndTime());
                            workDay.addTask(newTask);
                            return newTask;
                        }
                    }
                    WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                    workMonth.addWorkDay(workDay);
                    Task newTask = new Task(task.getNewTaskId());
                    newTask.setComment(task.getNewComment());
                    newTask.setStartTime(task.getNewStartTime());
                    newTask.setEndTime(task.getNewEndTime());
                    workDay.addTask(newTask);
                    return newTask;
                }
            }
            WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
            timeLogger.addMonth(workMonth);
            WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
            workMonth.addWorkDay(workDay);
            Task newTask = new Task(task.getNewTaskId());
            newTask.setComment(task.getNewComment());
            newTask.setStartTime(task.getNewStartTime());
            newTask.setEndTime(task.getNewEndTime());
            workDay.addTask(newTask);
            return newTask;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return new Task("0000");
    }

    @PUT
    @Path("/workmonths/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteTask(DeleteTaskRB task) {
        try {
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getDate().equals(YearMonth.of(task.getYear(), task.getMonth()))) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                            for (int i = 0; i < workDay.getTasks().size(); i++) {
                                if (task.getTaskId().equals(workDay.getTasks().get(i).getTaskId())
                                        && Task.stringToLocalTime(task.getStartTime()).equals(workDay.getTasks().get(i).getStartTime())) {
                                    workDay.getTasks().remove(i);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
