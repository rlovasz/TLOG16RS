package com.rozsalovasz.tlog16rs.resources.service;

import com.avaje.ebean.Ebean;
import com.rozsalovasz.tlog16rs.Util;
import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.WorkDayRB;
import com.rozsalovasz.tlog16rs.entities.Task;
import com.rozsalovasz.tlog16rs.entities.User;
import com.rozsalovasz.tlog16rs.entities.WorkDay;
import com.rozsalovasz.tlog16rs.entities.WorkMonth;
import com.rozsalovasz.tlog16rs.exceptions.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.exceptions.FutureWorkException;
import com.rozsalovasz.tlog16rs.exceptions.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.exceptions.NoTaskIdException;
import com.rozsalovasz.tlog16rs.exceptions.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.exceptions.NotNewDateException;
import com.rozsalovasz.tlog16rs.exceptions.NotNewMonthException;
import com.rozsalovasz.tlog16rs.exceptions.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.exceptions.NotTheSameMonthException;
import com.rozsalovasz.tlog16rs.exceptions.WeekendNotEnabledException;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TLOG16RSService {

    public Optional<WorkMonth> getSpecificMonth(List<WorkMonth> months, int year, int month) {
        return months.stream().filter(currentMonth -> currentMonth.getMonthDate().equals(YearMonth.of(year, month).toString())).findAny();
    }

    public Optional<WorkDay> getSpecificDay(List<WorkDay> days, int day) {
        return days.stream().filter(currentDay -> currentDay.getActualDay().getDayOfMonth() == day).findAny();
    }

    public Optional<Task> getSpecificTask(List<Task> tasks, LocalTime startTime) {
        return tasks.stream().filter(currentTask -> currentTask.getStartTime().equals(startTime)).findAny();
    }

    public WorkMonth addNewMonthToTimeLogger(User timeLogger, int year, int month) throws NotNewMonthException {
        WorkMonth workMonth = new WorkMonth(year, month);
        timeLogger.addMonth(workMonth);
        Ebean.save(timeLogger);
        return workMonth;
    }

    public void addWorkDayToTimeLogger(User timeLogger, WorkMonth workMonth, WorkDay workDay, boolean isWeekendEnabled) throws NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        workMonth.addWorkDay(workDay, isWeekendEnabled);
        calculateStatistics(workDay, workMonth);
        updateTimeLogger(workMonth, timeLogger);
    }

    public void addNewMonthAndDayToTimeLogger(WorkDayRB day, User timeLogger, boolean isWeekendEnabled) throws NotNewMonthException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = new WorkDay(Math.round(day.getRequiredHours() * 60), day.getYear(), day.getMonth(), day.getDay());
        addNewMonthAndDayHelper(timeLogger, workDay, day.getYear(), day.getMonth(), isWeekendEnabled);
    }

    public void addNewMonthAndDayToTimeLogger(int year, int month, int day, User timeLogger, boolean isWeekendEnabled) throws NotNewMonthException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = new WorkDay(year, month, day);
        addNewMonthAndDayHelper(timeLogger, workDay, year, month, isWeekendEnabled);
    }

    private void addNewMonthAndDayHelper(User timeLogger, WorkDay workDay, int year, int month, boolean isWeekendEnabled) throws NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException {
        WorkMonth workMonth = new WorkMonth(year, month);
        workMonth.addWorkDay(workDay, isWeekendEnabled);
        timeLogger.addMonth(workMonth);
        calculateStatistics(workDay, workMonth);
        Ebean.save(timeLogger);
    }

    public void addNewMonthDayAndTaskToTimeLogger(User timeLogger, StartTaskRB startTaskRB) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, ParseException {
        Task task = new Task(startTaskRB.getTaskId(), startTaskRB.getComment(), startTaskRB.getStartTime(), startTaskRB.getStartTime());
        addNewMonthDayAndTaskHelper(timeLogger, task, startTaskRB.getYear(), startTaskRB.getMonth(), startTaskRB.getDay());
    }

    public void addNewMonthDayAndTaskToTimeLogger(User timeLogger, FinishingTaskRB finishTaskRB) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, ParseException {
        Task task = new Task(finishTaskRB.getTaskId(), "", finishTaskRB.getStartTime(), finishTaskRB.getEndTime());
        addNewMonthDayAndTaskHelper(timeLogger, task, finishTaskRB.getYear(), finishTaskRB.getMonth(), finishTaskRB.getDay());
    }

    public void addNewMonthDayAndTaskToTimeLogger(User timeLogger, ModifyTaskRB modifyTaskRB) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NegativeMinutesOfWorkException, FutureWorkException, NotSeparatedTaskTimesException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
        Task task = new Task(modifyTaskRB.getNewTaskId(), modifyTaskRB.getNewComment(), modifyTaskRB.getNewStartTime(), modifyTaskRB.getNewEndTime());
        addNewMonthDayAndTaskHelper(timeLogger, task, modifyTaskRB.getYear(), modifyTaskRB.getMonth(), modifyTaskRB.getDay());
    }

    private void addNewMonthDayAndTaskHelper(User timeLogger, Task task, int year, int month, int day) throws NegativeMinutesOfWorkException, FutureWorkException, NotSeparatedTaskTimesException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException {
        WorkMonth workMonth = new WorkMonth(year, month);
        WorkDay workDay = new WorkDay(year, month, day);
        workDay.addTask(task);
        workMonth.addWorkDay(workDay, false);
        timeLogger.addMonth(workMonth);
        calculateStatistics(workDay, workMonth);
        Ebean.save(timeLogger);
    }

    public void addTaskToWorkDay(User timeLogger, WorkMonth workMonth, WorkDay workDay, Task task) throws NotSeparatedTaskTimesException {
        workDay.addTask(task);
        calculateStatistics(workDay, workMonth);
        updateTimeLogger(workMonth, timeLogger);
    }

    public void addNewDay(User timeLogger, WorkDayRB day, boolean isWeekendEnabled) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), day.getYear(), day.getMonth());
        if (workMonth.isPresent()) {
            WorkDay workDay = new WorkDay(Math.round((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            addWorkDayToTimeLogger(timeLogger, workMonth.get(), workDay, isWeekendEnabled);
        } else {
            addNewMonthAndDayToTimeLogger(day, timeLogger, isWeekendEnabled);
        }
    }

    public List<WorkDay> listSpecificMonth(User timeLogger, int year, int month) throws NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), year, month);
        List<WorkDay> days;
        if (workMonth.isPresent()) {
            days = workMonth.get().getDays();
        } else {
            days = addNewMonthToTimeLogger(timeLogger, year, month).getDays();
        }
        return days;
    }

    public List<Task> listSpecificDay(User timeLogger, int year, int month, int day) throws NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), year, month);
        List<Task> tasks;
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), day);
            if (workDay.isPresent()) {
                tasks = workDay.get().getTasks();
            } else {
                addWorkDayToTimeLogger(timeLogger, workMonth.get(), new WorkDay(year, month, day), false);
                tasks = new ArrayList<>();
            }
        } else {
            WorkDay workDay = new WorkDay(year, month, day);
            addNewMonthAndDayToTimeLogger(year, month, day, timeLogger, false);
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    public void startNewTask(User timeLogger, StartTaskRB task) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
        Task startedTask = new Task(task.getTaskId(), task.getComment(), task.getStartTime(), task.getStartTime());
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                addTaskToWorkDay(timeLogger, workMonth.get(), workDay.get(), startedTask);
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                addTaskToWorkDay(timeLogger, workMonth.get(), newDay, startedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(timeLogger, task);
        }
    }

    public void finishStartedTask(User timeLogger, FinishingTaskRB task) throws EmptyTimeFieldException, NotExpectedTimeOrderException, NoTaskIdException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, InvalidTaskIdException, ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                List<Task> tasks = workDay.get().getTasks();
                Optional<Task> currentTask = getSpecificTask(tasks, Util.parseString(task.getStartTime()));
                if (currentTask.isPresent()) {
                    List< Task> testTasks = new ArrayList<>();
                    Task testTask = new Task(currentTask.get().getTaskId(), currentTask.get().getComment(), currentTask.get().getStartTime().toString(), currentTask.get().getEndTime().toString());
                    int matchIndex = tasks.indexOf(currentTask.get());
                    for (int i = 0; i < tasks.size(); i++) {
                        if (i != matchIndex) {
                            testTasks.add(tasks.get(i));
                        }
                    }
                    testTask.setEndTime(task.getEndTime());
                    if (!Util.isSeparatedTime(testTasks, testTask)) {
                        throw new NotSeparatedTaskTimesException("");
                    } else {
                        currentTask.get().setEndTime(testTask.getEndTime());
                    }

                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.update(workMonth.get());
                } else {
                    Task newTask = new Task(task.getTaskId(), "", task.getStartTime(), task.getEndTime());
                    addTaskToWorkDay(timeLogger, workMonth.get(), workDay.get(), newTask);
                }
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                Task finishedTask = new Task(task.getTaskId(), "", task.getStartTime(), task.getEndTime());
                addTaskToWorkDay(timeLogger, workMonth.get(), newDay, finishedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(timeLogger, task);
        }
    }

    public void modifyExistingTask(User timeLogger, ModifyTaskRB task) throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                List<Task> tasks = workDay.get().getTasks();
                Optional<Task> currentTask = getSpecificTask(tasks, Util.parseString(task.getStartTime()));
                if (currentTask.isPresent()) {
                    currentTask.get().setTaskId(task.getNewTaskId());
                    currentTask.get().setComment(task.getNewComment());
                    List< Task> testTasks = new ArrayList<>();
                    Task testTask = new Task(currentTask.get().getTaskId(), currentTask.get().getComment(), currentTask.get().getStartTime().toString(), currentTask.get().getEndTime().toString());
                    int matchIndex = tasks.indexOf(currentTask.get());
                    for (int i = 0; i < tasks.size(); i++) {
                        if (i != matchIndex) {
                            testTasks.add(tasks.get(i));
                        }
                    }
                    testTask.setEndTime(task.getNewEndTime());
                    testTask.setStartTime(task.getNewStartTime());
                    if (!Util.isSeparatedTime(testTasks, testTask)) {
                        throw new NotSeparatedTaskTimesException("");
                    } else {
                        currentTask.get().setStartTime(task.getNewStartTime());
                        currentTask.get().setEndTime(task.getNewEndTime());
                    }
                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.update(workMonth.get());
                } else {
                    Task newTask = new Task(task.getNewTaskId(), task.getNewComment(), task.getNewStartTime(), task.getNewEndTime());
                    addTaskToWorkDay(timeLogger, workMonth.get(), workDay.get(), newTask);
                }
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                Task finishedTask = new Task(task.getNewTaskId(), task.getNewComment(), task.getNewStartTime(), task.getNewEndTime());
                addTaskToWorkDay(timeLogger, workMonth.get(), newDay, finishedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(timeLogger, task);
        }
    }

    public void deleteTask(User timeLogger, DeleteTaskRB task) throws ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                Optional<Task> currentTask = getSpecificTask(workDay.get().getTasks(), Util.parseString(task.getStartTime()));
                if (currentTask.isPresent()) {
                    Ebean.delete(currentTask.get());
                    workDay.get().removeTask(currentTask.get());
                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.save(timeLogger);
                }
            }
        }
    }

    public void calculateStatistics(WorkDay workDay, WorkMonth workMonth) {
        workMonth.getRequiredMinPerMonth();
        workDay.getExtraMinPerDay();
        workMonth.getExtraMinPerMonth();
    }

    private void updateTimeLogger(WorkMonth workMonth, User timeLogger) {
        Ebean.save(workMonth);
        Ebean.update(timeLogger);
    }
}
