package com.rozsalovasz.tlog16rs.resources.service;

import com.avaje.ebean.Ebean;
import com.google.common.hash.Hashing;
import com.rozsalovasz.tlog16rs.Util;
import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.UserRB;
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
import io.dropwizard.auth.AuthenticationException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotAuthorizedException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

public class TLOG16RSService {

    JwtService jwtService = new JwtService();

    /**
     * Creates a new month for the user
     *
     * @param user
     * @param year
     * @param month
     * @return with the created month
     * @throws NotNewMonthException
     */
    public WorkMonth addNewMonthToTimeLogger(User user, int year, int month) throws NotNewMonthException {
        WorkMonth workMonth = new WorkMonth(year, month);
        user.addMonth(workMonth);
        Ebean.save(user);
        return workMonth;
    }

    /**
     * Adds new day for the given month
     *
     * @param timeLogger
     * @param day
     * @param isWeekendEnabled only creates the new day if it is on weekday or
     * if weekends are enabled
     * @throws NegativeMinutesOfWorkException
     * @throws FutureWorkException
     * @throws NotNewDateException
     * @throws NotTheSameMonthException
     * @throws WeekendNotEnabledException
     * @throws NotNewMonthException
     */
    public void addNewDay(User timeLogger, WorkDayRB day, boolean isWeekendEnabled) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(timeLogger.getMonths(), day.getYear(), day.getMonth());
        if (workMonth.isPresent()) {
            WorkDay workDay = new WorkDay(Math.round((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            addWorkDayToTimeLogger(timeLogger, workMonth.get(), workDay, isWeekendEnabled);
        } else {
            addNewMonthAndDayToTimeLogger(day, timeLogger, isWeekendEnabled);
        }
    }

    /**
     * Lists the days of the specified month
     *
     * @param user
     * @param year
     * @param month
     * @return with the list of days
     * @throws NotNewMonthException
     */
    public List<WorkDay> listSpecificMonth(User user, int year, int month) throws NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), year, month);
        List<WorkDay> days;
        if (workMonth.isPresent()) {
            days = workMonth.get().getDays();
        } else {
            days = addNewMonthToTimeLogger(user, year, month).getDays();
        }
        return days;
    }

    /**
     * Lists the tasks of the specified day
     *
     * @param user
     * @param year
     * @param month
     * @param day
     * @return with the list of tasks
     * @throws NotNewDateException
     * @throws NotTheSameMonthException
     * @throws WeekendNotEnabledException
     * @throws NegativeMinutesOfWorkException
     * @throws FutureWorkException
     * @throws NotNewMonthException
     */
    public List<Task> listSpecificDay(User user, int year, int month, int day) throws NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException, NotNewMonthException {
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), year, month);
        List<Task> tasks;
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), day);
            if (workDay.isPresent()) {
                tasks = workDay.get().getTasks();
            } else {
                addWorkDayToTimeLogger(user, workMonth.get(), new WorkDay(year, month, day), false);
                tasks = new ArrayList<>();
            }
        } else {
            WorkDay workDay = new WorkDay(year, month, day);
            addNewMonthAndDayToTimeLogger(year, month, day, user, false);
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    /**
     * Starts a new task with the given data
     *
     * @param user
     * @param task
     * @throws NoTaskIdException
     * @throws InvalidTaskIdException
     * @throws EmptyTimeFieldException
     * @throws NotExpectedTimeOrderException
     * @throws NotSeparatedTaskTimesException
     * @throws NegativeMinutesOfWorkException
     * @throws FutureWorkException
     * @throws NotNewDateException
     * @throws NotTheSameMonthException
     * @throws WeekendNotEnabledException
     * @throws NotNewMonthException
     * @throws ParseException
     */
    public void startNewTask(User user, StartTaskRB task) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
        Task startedTask = new Task(task.getTaskId(), task.getComment(), task.getStartTime(), task.getStartTime());
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                addTaskToWorkDay(user, workMonth.get(), workDay.get(), startedTask);
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                addTaskToWorkDay(user, workMonth.get(), newDay, startedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(user, task);
        }
    }

    /**
     * Finishes a started task or create it with the given data if it is not
     * exists
     *
     * @param user
     * @param task
     * @throws EmptyTimeFieldException
     * @throws NotExpectedTimeOrderException
     * @throws NoTaskIdException
     * @throws NotSeparatedTaskTimesException
     * @throws NegativeMinutesOfWorkException
     * @throws FutureWorkException
     * @throws NotNewDateException
     * @throws NotTheSameMonthException
     * @throws WeekendNotEnabledException
     * @throws NotNewMonthException
     * @throws InvalidTaskIdException
     * @throws ParseException
     */
    public void finishStartedTask(User user, FinishingTaskRB task) throws EmptyTimeFieldException, NotExpectedTimeOrderException, NoTaskIdException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, InvalidTaskIdException, ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                List<Task> tasks = workDay.get().getTasks();
                Optional<Task> currentTask = getSpecificTask(tasks, Util.parseStringTime(task.getStartTime()));
                if (currentTask.isPresent()) {
                    boolean separated = checkSeparation(currentTask, tasks, task);
                    if (!separated) {
                        throw new NotSeparatedTaskTimesException("");
                    } else {
                        currentTask.get().setEndTime(task.getEndTime());
                    }
                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.update(workMonth.get());
                } else {
                    Task newTask = new Task(task.getTaskId(), "", task.getStartTime(), task.getEndTime());
                    addTaskToWorkDay(user, workMonth.get(), workDay.get(), newTask);
                }
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                Task finishedTask = new Task(task.getTaskId(), "", task.getStartTime(), task.getEndTime());
                addTaskToWorkDay(user, workMonth.get(), newDay, finishedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(user, task);
        }
    }

    /**
     * Does the modifications on existing task or create it with the given
     * values
     *
     * @param user
     * @param task
     * @throws InvalidTaskIdException
     * @throws NoTaskIdException
     * @throws EmptyTimeFieldException
     * @throws NotExpectedTimeOrderException
     * @throws NotSeparatedTaskTimesException
     * @throws NegativeMinutesOfWorkException
     * @throws FutureWorkException
     * @throws NotNewDateException
     * @throws NotTheSameMonthException
     * @throws WeekendNotEnabledException
     * @throws NotNewMonthException
     * @throws ParseException
     */
    public void modifyExistingTask(User user, ModifyTaskRB task) throws InvalidTaskIdException, NoTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                List<Task> tasks = workDay.get().getTasks();
                Optional<Task> currentTask = getSpecificTask(tasks, Util.parseStringTime(task.getStartTime()));
                if (currentTask.isPresent()) {
                    modifyTheChosenTask(currentTask, task, tasks);
                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.update(workMonth.get());
                } else {
                    Task newTask = new Task(task.getNewTaskId(), task.getNewComment(), task.getNewStartTime(), task.getNewEndTime());
                    addTaskToWorkDay(user, workMonth.get(), workDay.get(), newTask);
                }
            } else {
                WorkDay newDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
                workMonth.get().addWorkDay(newDay);
                Task finishedTask = new Task(task.getNewTaskId(), task.getNewComment(), task.getNewStartTime(), task.getNewEndTime());
                addTaskToWorkDay(user, workMonth.get(), newDay, finishedTask);
            }
        } else {
            addNewMonthDayAndTaskToTimeLogger(user, task);
        }
    }

    /**
     * Deletes the given task of the given user
     *
     * @param user
     * @param task
     * @throws ParseException
     */
    public void deleteTask(User user, DeleteTaskRB task) throws ParseException {
        Optional<WorkMonth> workMonth = getSpecificMonth(user.getMonths(), task.getYear(), task.getMonth());
        if (workMonth.isPresent()) {
            Optional<WorkDay> workDay = getSpecificDay(workMonth.get().getDays(), task.getDay());
            if (workDay.isPresent()) {
                Optional<Task> currentTask = getSpecificTask(workDay.get().getTasks(), Util.parseStringTime(task.getStartTime()));
                if (currentTask.isPresent()) {
                    Ebean.delete(currentTask.get());
                    workDay.get().removeTask(currentTask.get());
                    calculateStatistics(workDay.get(), workMonth.get());
                    Ebean.save(user);
                }
            }
        }
    }

    /**
     * Calculates the statistics for the month and the day
     *
     * @param workDay
     * @param workMonth
     */
    public void calculateStatistics(WorkDay workDay, WorkMonth workMonth) {
        workMonth.getRequiredMinPerMonth();
        workDay.getExtraMinPerDay();
        workMonth.getExtraMinPerMonth();
    }

    /**
     * Saves the user to database with generated salt and encodes the password
     *
     * @param user
     */
    public void registerUser(UserRB user) {
        String salt = generateSalt();
        String password = Hashing.sha256().hashString(user.getPassword() + salt, StandardCharsets.UTF_8).toString();
        User newUser = new User(user.getName(), password, salt);
        Ebean.save(newUser);
    }

    /**
     * Login with the user by generating a jwt
     *
     * @param user
     * @return with the generated jwt
     * @throws UnsupportedEncodingException
     * @throws JoseException
     * @throws AuthenticationException
     */
    public String loginUser(UserRB user) throws UnsupportedEncodingException, JoseException, AuthenticationException {
        String jwt;
        User existingUser = Ebean.find(User.class).where().eq("name", user.getName()).findUnique();
        if (existingUser != null) {
            String password = Hashing.sha256().hashString(user.getPassword() + existingUser.getSalt(), StandardCharsets.UTF_8).toString();
            if (password.equals(existingUser.getPassword())) {
                jwt = jwtService.generateJwtToken(existingUser);
            } else {
                throw new AuthenticationException("User not exists");
            }
        } else {
            throw new AuthenticationException("User not exists");
        }
        return jwt;
    }

    /**
     * Returns with the user logged in with the given token
     *
     * @param token, jwt token
     * @return with the user logged in with the given token
     * @throws NotAuthorizedException
     * @throws UnsupportedEncodingException
     * @throws JoseException
     * @throws InvalidJwtException
     */
    public User getUser(String token) throws NotAuthorizedException, UnsupportedEncodingException, JoseException, InvalidJwtException {
        if (token != null) {
            String jwt = token.split(" ")[1];
            String name = jwtService.getNameFromJwtToken(jwt);
            User user = Ebean.find(User.class).where().eq("name", name).findUnique();
            return user;
        } else {
            throw new NotAuthorizedException("Not existing user");
        }
    }

    private void updateTimeLogger(WorkMonth workMonth, User timeLogger) {
        Ebean.save(workMonth);
        Ebean.update(timeLogger);
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        String salt = new BigInteger(25, random).toString(32);
        return salt;
    }

    private void modifyTheChosenTask(Optional<Task> currentTask, ModifyTaskRB task, List<Task> tasks) throws InvalidTaskIdException, NotSeparatedTaskTimesException, NoTaskIdException, ParseException, EmptyTimeFieldException, NotExpectedTimeOrderException {
        currentTask.get().setTaskId(task.getNewTaskId());
        currentTask.get().setComment(task.getNewComment());
        boolean separated = checkSeparation(currentTask, tasks, task);
        if (!separated) {
            throw new NotSeparatedTaskTimesException("");
        } else {
            currentTask.get().setStartTime(task.getNewStartTime());
            currentTask.get().setEndTime(task.getNewEndTime());
        }
    }

    private boolean checkSeparation(Optional<Task> currentTask, List<Task> tasks, FinishingTaskRB task) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, ParseException {
        List< Task> testTasks = checkSeparationHelper(tasks, currentTask);
        Task testTask = new Task(currentTask.get().getTaskId(), currentTask.get().getComment(), currentTask.get().getStartTime().toString(), currentTask.get().getEndTime().toString());
        testTask.setEndTime(task.getEndTime());
        boolean separated = Util.isSeparatedTime(testTasks, testTask);
        return separated;
    }

    private boolean checkSeparation(Optional<Task> currentTask, List<Task> tasks, ModifyTaskRB task) throws ParseException, InvalidTaskIdException, EmptyTimeFieldException, NoTaskIdException, NotExpectedTimeOrderException {
        Task testTask = new Task(currentTask.get().getTaskId(), currentTask.get().getComment(), currentTask.get().getStartTime().toString(), currentTask.get().getEndTime().toString());
        List<Task> testTasks = checkSeparationHelper(tasks, currentTask);
        testTask.setEndTime(task.getNewEndTime());
        testTask.setStartTime(task.getNewStartTime());
        boolean separated = Util.isSeparatedTime(testTasks, testTask);
        return separated;
    }

    private List<Task> checkSeparationHelper(List<Task> tasks, Optional<Task> currentTask) {
        List< Task> testTasks = new ArrayList<>();
        int matchIndex = tasks.indexOf(currentTask.get());
        for (int i = 0; i < tasks.size(); i++) {
            if (i != matchIndex) {
                testTasks.add(tasks.get(i));
            }
        }
        return testTasks;
    }

    private Optional<WorkMonth> getSpecificMonth(List<WorkMonth> months, int year, int month) {
        return months.stream().filter(currentMonth -> currentMonth.getMonthDate().equals(YearMonth.of(year, month).toString())).findAny();
    }

    private Optional<WorkDay> getSpecificDay(List<WorkDay> days, int day) {
        return days.stream().filter(currentDay -> currentDay.getActualDay().getDayOfMonth() == day).findAny();
    }

    private Optional<Task> getSpecificTask(List<Task> tasks, LocalTime startTime) {
        return tasks.stream().filter(currentTask -> currentTask.getStartTime().equals(startTime)).findAny();
    }

    private void addWorkDayToTimeLogger(User timeLogger, WorkMonth workMonth, WorkDay workDay, boolean isWeekendEnabled) throws NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException {
        workMonth.addWorkDay(workDay, isWeekendEnabled);
        calculateStatistics(workDay, workMonth);
        updateTimeLogger(workMonth, timeLogger);
    }

    private void addNewMonthAndDayToTimeLogger(WorkDayRB day, User timeLogger, boolean isWeekendEnabled) throws NotNewMonthException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException {
        WorkDay workDay = new WorkDay(Math.round(day.getRequiredHours() * 60), day.getYear(), day.getMonth(), day.getDay());
        addNewMonthAndDayHelper(timeLogger, workDay, day.getYear(), day.getMonth(), isWeekendEnabled);
    }

    private void addNewMonthAndDayToTimeLogger(int year, int month, int day, User timeLogger, boolean isWeekendEnabled) throws NotNewMonthException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NegativeMinutesOfWorkException, FutureWorkException {
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

    private void addNewMonthDayAndTaskToTimeLogger(User timeLogger, StartTaskRB startTaskRB) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, ParseException {
        Task task = new Task(startTaskRB.getTaskId(), startTaskRB.getComment(), startTaskRB.getStartTime(), startTaskRB.getStartTime());
        addNewMonthDayAndTaskHelper(timeLogger, task, startTaskRB.getYear(), startTaskRB.getMonth(), startTaskRB.getDay());
    }

    private void addNewMonthDayAndTaskToTimeLogger(User timeLogger, FinishingTaskRB finishTaskRB) throws NegativeMinutesOfWorkException, FutureWorkException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NotSeparatedTaskTimesException, ParseException {
        Task task = new Task(finishTaskRB.getTaskId(), "", finishTaskRB.getStartTime(), finishTaskRB.getEndTime());
        addNewMonthDayAndTaskHelper(timeLogger, task, finishTaskRB.getYear(), finishTaskRB.getMonth(), finishTaskRB.getDay());
    }

    private void addNewMonthDayAndTaskToTimeLogger(User timeLogger, ModifyTaskRB modifyTaskRB) throws NoTaskIdException, InvalidTaskIdException, EmptyTimeFieldException, NotExpectedTimeOrderException, NegativeMinutesOfWorkException, FutureWorkException, NotSeparatedTaskTimesException, NotNewDateException, NotTheSameMonthException, WeekendNotEnabledException, NotNewMonthException, ParseException {
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

    private void addTaskToWorkDay(User timeLogger, WorkMonth workMonth, WorkDay workDay, Task task) throws NotSeparatedTaskTimesException {
        workDay.addTask(task);
        calculateStatistics(workDay, workMonth);
        updateTimeLogger(workMonth, timeLogger);
    }
}
