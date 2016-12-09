package com.rozsalovasz.tlog16rs.resources;

import com.avaje.ebean.Ebean;
import com.google.common.hash.Hashing;
import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.UserRB;
import com.rozsalovasz.tlog16rs.beans.WorkDayRB;
import com.rozsalovasz.tlog16rs.beans.WorkMonthRB;
import com.rozsalovasz.tlog16rs.core.FutureWorkException;
import com.rozsalovasz.tlog16rs.core.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException;
import com.rozsalovasz.tlog16rs.core.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.core.WeekendNotEnabledException;
import com.rozsalovasz.tlog16rs.entities.Task;
import com.rozsalovasz.tlog16rs.entities.TimeLogger;
import com.rozsalovasz.tlog16rs.entities.WorkDay;
import com.rozsalovasz.tlog16rs.entities.WorkMonth;
import io.dropwizard.auth.AuthenticationException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

/**
 * This is the class where the REST endpoint are written
 *
 * @author rlovasz
 */
@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(UserRB user) {
        Key key = null;
        boolean matching = false;
        try {
            for (TimeLogger timeLogger : Ebean.find(TimeLogger.class).findList()) {
                String password = Hashing.sha256().hashString(user.getPassword() + timeLogger.getSalt(), StandardCharsets.UTF_8).toString();
                if (timeLogger.getName().equals(user.getName()) && password.equals(timeLogger.getPassword())) {
                    key = new HmacKey(password.getBytes("UTF-8"));
                    matching = true;
                }
            }
            if (matching == false) {
                throw new AuthenticationException("User not exists");
            }
            JwtClaims claims = new JwtClaims();
            JsonWebSignature jws = new JsonWebSignature();
            String jwt;
            claims.setSubject(user.getName());
            jws.setPayload(claims.toJson());
            jws.setKey(key);
            jws.setKeyIdHeaderValue("kid");
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setDoKeyValidation(false);
            jwt = jws.getCompactSerialization();
            return Response.status(Response.Status.OK).header("Authorization", "Bearer " + jwt).header("Access-Control-Expose-Headers", "Authorization").build();
        } catch (UnsupportedEncodingException | AuthenticationException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserRB user) {
        try {
            boolean exists = false;
            Response response;
            for (TimeLogger timeLogger : Ebean.find(TimeLogger.class).findList()) {
                if (timeLogger.getName().equals(user.getName())) {
                    exists = true;
                }
            }
            if (exists == true) {
                response = Response.status(Response.Status.CONFLICT).build();
            } else {
                SecureRandom random = new SecureRandom();
                String salt = new BigInteger(25, random).toString(32);
                String password = Hashing.sha256().hashString(user.getPassword() + salt, StandardCharsets.UTF_8).toString();
                TimeLogger timelogger = new TimeLogger(user.getName(), password, salt);
                Ebean.save(timelogger);
                response = Response.status(Response.Status.OK).build();
            }
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /**
     * This is a POST method to add a new working month to the database
     *
     * @param month is a RestBean, where are the important data to create the
     * new WorkMonth (year, month)
     * @param token
     * @return with the created WorkMonth object
     */
    @POST
    @Path("/workmonths")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewMonth(WorkMonthRB month, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            return Response.ok(addNewMonthAndSaveTimeLogger(month.getYear(), month.getMonth(), timeLogger)).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a GET method to list all the existing months of the database
     *
     * @param token
     * @return the list of WorkMonth objects
     */
    @GET
    @Path("/workmonths")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listWorkMonths(@HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            return Response.ok(timeLogger.getMonths()).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a PUT method to delete the whole database
     *
     * @return
     */
    @PUT
    @Path("/workmonths/deleteall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllMonths() {
        int i = 1;
        while (!Ebean.find(TimeLogger.class).findList().isEmpty()) {
            Ebean.delete(TimeLogger.class, i);
            i++;
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * This is a GET method, which displays the list of WorkDays in the month
     * which is specifies in the path
     *
     * @param year
     * @param month
     * @param token
     * @return with the list of WorkDay objects
     */
    @GET
    @Path("/workmonths/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listSpecificMonth(@PathParam("year") int year, @PathParam("month") int month, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(year, month).toString())) {
                    return Response.ok(workMonth.getDays()).build();
                }
            }
            WorkMonth workMonth = addNewMonthAndSaveTimeLogger(year, month, timeLogger);
            return Response.ok(workMonth.getDays()).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a POST method, which saves a new work day into the database,
     * which is specified in the WorkDayRB type parameter. If the work month is
     * not created yet, it creates it.
     *
     * @param day Specifies the day to create with the following data: year,
     * month, day, requiredHours
     * @param token
     * @return with the created WorkDay object
     */
    @POST
    @Path("/workmonths/workdays")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewDay(WorkDayRB day, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(workDay.getActualDay().getYear(), workDay.getActualDay().getMonthValue()).toString())) {
                    addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay, timeLogger);
                    return Response.status(Response.Status.OK).build();
                }
            }
            addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
            return Response.status(Response.Status.OK).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage());
            return Response.status(428).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage());
            return Response.status(449).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a POST method, which saves a new work day on the weekend into the
     * database, which is specified in the WorkDayRB type parameter. If the work
     * month is not created yet, it creates it.
     *
     * @param day Specifies the weekend day to create with the following data:
     * year, month, day, requiredHours
     * @param token
     * @return with the created WorkDay object
     */
    @POST
    @Path("/workmonths/workdays/weekend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addNewDayWeekend(WorkDayRB day, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(workDay.getActualDay().getYear(), workDay.getActualDay().getMonthValue()).toString())) {
                    addWeekendWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay, timeLogger);
                    return Response.status(Response.Status.OK).build();
                }
            }
            addNewMonthAndWeekendDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
            return Response.status(Response.Status.OK).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage());
            return Response.status(449).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @PUT
    @Path("/workmonths/workdays/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyRequiredMinPerDay(WorkDayRB day, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(day.getYear(), day.getMonth()).toString())) {
                    for (WorkDay workDay : Ebean.find(WorkDay.class).findList()) {
                        if (workDay.getActualDay().equals(LocalDate.of(day.getYear(), day.getMonth(), day.getDay()))) {
                            workDay.setRequiredMinPerDay((int) (day.getRequiredHours() * 60));
                            getStatistics(workDay, workMonth);
                            Ebean.save(workDay);
                            Ebean.update(timeLogger);
                            return Response.status(Response.Status.OK).build();
                        }

                    }
                    WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
                    workMonth.addWorkDay(workDay);
                    getStatistics(workDay, workMonth);
                    saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
                    return Response.status(Response.Status.OK).build();
                }
            }
            WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
            addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
            return Response.status(Response.Status.OK).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage());
            return Response.status(449).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /**
     * This is a GET method, which displays the list of tasks of the day
     * specified by the parameters in the path
     *
     * @param year
     * @param month
     * @param day
     * @param token
     * @return with the list of Task objects
     */
    @GET
    @Path("/workmonths/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listSpecificDay(@PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(year, month).toString())) {
                    return Response.ok(getTasksOfSpecifiedDayOrEmptyListIfNotExist(workMonth, day, year, month, timeLogger)).build();
                }
            }
            WorkDay workDay = new WorkDay(year, month, day);
            addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(year, month, timeLogger, workDay);
            return Response.ok(workDay.getTasks()).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a POST method, which creates a new Task in the database, where
     * the endTime is not known yet. If the day or the month is not created yet,
     * then it creates them.
     *
     * @param task This is a StartTaskRB type object, it has the following
     * required fields: int year, int month, int day, String taskId, String
     * comment, String startTime
     * @param token
     * @return with the Task object
     */
    @POST
    @Path("/workmonths/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startNewTask(StartTaskRB task, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            Task startedTask = new Task(task.getTaskId());
            startedTask.setComment(task.getComment());
            startedTask.setStartTime(task.getStartTime());
            startedTask.setEndTime(task.getStartTime());
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
                    return getTheSpecifiedDayExistThenAddTheTask(workMonth, task, startedTask, timeLogger);
                } 
            }
            createMonthAndDayThenAddTask(task, startedTask, timeLogger);
            return Response.status(Response.Status.OK).build();
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }

    /**
     * This is a PUT object, which finishes a started Task with the information
     * of endTime. If the month, the day or the task doesn't exist yet, it
     * created them with the given informations.
     *
     * @param task it is a FinishingTaskRB object, it has the following required
     * fields: int year, int month, int day, String taskId, String startTime,
     * String endTime
     * @param token
     * @return with the finished Task object
     */
    @PUT
    @Path("/workmonths/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response finishStartedTask(FinishingTaskRB task, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            return getTheContainingMonthAndDayAndFinishTaskOrCreateMonthDayAndTask(task, timeLogger);
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (NotExpectedTimeOrderException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        } catch (NotMultipleQuarterHourException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a PUT method to modify any property of the specified Task. If the
     * month, the day or the task doesn't exist, it will create them with the
     * given informations.
     *
     * @param task This is a ModifyTaskRB type object, which has the following
     * required fields: int year, int month, int day, String taskId, String
     * startTime, String newTaskId, String newComment, String newStartTime,
     * String newEndTime
     * @param token
     * @return with the modified Task object
     */
    @PUT
    @Path("/workmonths/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyExistingTask(ModifyTaskRB task, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            return getContainingMonthAndModifyTaskOrCreateMonthDayAndTask(task, timeLogger);
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.CONFLICT).build();
        } catch (NotExpectedTimeOrderException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        } catch (NotMultipleQuarterHourException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This is a PUT method to delete an existing Task.
     *
     * @param task This is a DeleteTaskRB type object, which has the following
     * required fields: int year, int month, int day, String taskId, String
     * startTime
     * @param token
     */
    @PUT
    @Path("/workmonths/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTask(DeleteTaskRB task, @HeaderParam("Authorization") String token) {
        try {
            TimeLogger timeLogger = getTimeLogger(token);
            for (WorkMonth workMonth : timeLogger.getMonths()) {
                if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
                    getDayAndDeleteItsSpecifiedTask(workMonth, task, timeLogger);
                }
            }
            return Response.status(Response.Status.OK).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private void getDayAndDeleteItsSpecifiedTask(WorkMonth workMonth, DeleteTaskRB task, TimeLogger timeLogger) {
        for (WorkDay workDay : workMonth.getDays()) {
            if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                getSpecifiedTaskEndDeleteIt(workDay, task, workMonth, timeLogger);
            }
        }
    }

    private void getSpecifiedTaskEndDeleteIt(WorkDay workDay, DeleteTaskRB task, WorkMonth workMonth, TimeLogger timeLogger) {
        for (int i = 0; i < workDay.getTasks().size(); i++) {
            if (task.getTaskId().equals(workDay.getTasks().get(i).getTaskId())
                    && Task.stringToLocalTime(task.getStartTime()).equals(workDay.getTasks().get(i).getStartTime())) {
                Ebean.delete(Task.class, i + 1);
                Ebean.delete(workDay.getTasks().get(i));
                workDay.removeTask(workDay.getTasks().get(i));
                getStatistics(workDay, workMonth);
                Ebean.save(timeLogger);
            }
        }
    }

    private void getStatistics(WorkDay workDay, WorkMonth workMonth) {
        workDay.getRequiredMinPerDay();
        workDay.getExtraMinPerDay();
        workMonth.getRequiredMinPerMonth();
        workMonth.getExtraMinPerMonth();
    }

    private WorkMonth addNewMonthAndSaveTimeLogger(int year, int month, TimeLogger timeLogger) {
        WorkMonth workMonth = new WorkMonth(year, month);
        timeLogger.addMonth(workMonth);
        Ebean.save(timeLogger);
        return workMonth;
    }

    private void addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(int year, int month, TimeLogger timeLogger, WorkDay workDay) {
        WorkMonth workMonth = new WorkMonth(year, month);
        timeLogger.addMonth(workMonth);
        workMonth.addWorkDay(workDay);
        getStatistics(workDay, workMonth);
        Ebean.save(timeLogger);
    }

    private void addNewMonthAndWeekendDayGetStaticsticsAndSaveTimeLogger(int year, int month, TimeLogger timeLogger, WorkDay workDay) {
        WorkMonth workMonth = new WorkMonth(year, month);
        timeLogger.addMonth(workMonth);
        workMonth.addWorkDay(workDay, true);
        getStatistics(workDay, workMonth);
        Ebean.save(timeLogger);
    }

    private void addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(WorkMonth workMonth, WorkDay workDay, TimeLogger timeLogger) {
        workMonth.addWorkDay(workDay);
        getStatistics(workDay, workMonth);
        saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
    }

    private void addWeekendWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(WorkMonth workMonth, WorkDay workDay, TimeLogger timeLogger) {
        workMonth.addWorkDay(workDay, true);
        getStatistics(workDay, workMonth);
        saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
    }

    private void createTaskAddToWorkDayGetStatisticsAndSave(FinishingTaskRB task, WorkDay workDay, WorkMonth workMonth) {
        Task startedTask = new Task(task.getTaskId());
        startedTask.setStartTime(task.getStartTime());
        startedTask.setEndTime(task.getEndTime());
        workDay.addTask(startedTask);
        getStatistics(workDay, workMonth);
        Ebean.save(workMonth);
    }

    private Task setModifiedProperties(Task existingTask, ModifyTaskRB task) {
        existingTask.setComment(task.getNewComment());
        existingTask.setStartTime(task.getNewStartTime());
        existingTask.setEndTime(task.getNewEndTime());
        return existingTask;
    }

    private void saveWorkMonthUpdateTimeLogger(WorkMonth workMonth, TimeLogger timeLogger) {
        Ebean.save(workMonth);
        Ebean.update(timeLogger);
    }

    private void addMonthDayTaskGetStatisticsAndSave(TimeLogger timeLogger, WorkMonth workMonth, WorkDay workDay, Task startedTask) {
        timeLogger.addMonth(workMonth);
        workMonth.addWorkDay(workDay);
        workDay.addTask(startedTask);
        getStatistics(workDay, workMonth);
        Ebean.save(timeLogger);
    }

    private void createMonthAndDayThenAddTask(StartTaskRB task, Task startedTask, TimeLogger timeLogger) {
        WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        addMonthDayTaskGetStatisticsAndSave(timeLogger, workMonth, workDay, startedTask);
    }
    
    private Response getTheSpecifiedDayExistThenAddTheTask(WorkMonth workMonth, StartTaskRB task, Task startedTask, TimeLogger timeLogger) {
        for (WorkDay workDay : workMonth.getDays()) {
            if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                workDay.addTask(startedTask);
                getStatistics(workDay, workMonth);
                saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
                return Response.ok().build();
            }
        }
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        workMonth.addWorkDay(workDay);
        workDay.addTask(startedTask);
        getStatistics(workDay, workMonth);
        saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
        return Response.ok().build();
    }

    private List<Task> getTasksOfSpecifiedDayOrEmptyListIfNotExist(WorkMonth workMonth, int day, int year, int month, TimeLogger timeLogger) {
        for (WorkDay workDay : workMonth.getDays()) {
            if (workDay.getActualDay().getDayOfMonth() == day) {
                return workDay.getTasks();
            }
        }
        WorkDay workDay = new WorkDay(year, month, day);
        addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay, timeLogger);
        return workDay.getTasks();
    }

    private Response getTheContainingMonthAndDayAndFinishTaskOrCreateMonthDayAndTask(FinishingTaskRB task, TimeLogger timeLogger) {
        Response response;
        for (WorkMonth workMonth : timeLogger.getMonths()) {
            if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
                return getSpecifiedDayAndFinishTheTaskOrCreateDayAndTask(workMonth, task);
            }
        }
        WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        Task startedTask = new Task(task.getTaskId());
        startedTask.setStartTime(task.getStartTime());
        startedTask.setEndTime(task.getEndTime());
        addMonthDayTaskGetStatisticsAndSave(timeLogger, workMonth, workDay, startedTask);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private Response getSpecifiedDayAndFinishTheTaskOrCreateDayAndTask(WorkMonth workMonth, FinishingTaskRB task) {
        Response response;
        for (WorkDay workDay : workMonth.getDays()) {
            if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                return getSpecifiedTaskOrCreateIfNotExist(workDay, task, workMonth);
            }
        }
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        workMonth.addWorkDay(workDay);
        createTaskAddToWorkDayGetStatisticsAndSave(task, workDay, workMonth);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private Response getSpecifiedTaskOrCreateIfNotExist(WorkDay workDay, FinishingTaskRB task, WorkMonth workMonth) {
        Response response;
        for (Task startedTask : workDay.getTasks()) {
            if (task.getTaskId().equals(startedTask.getTaskId())
                    && Task.stringToLocalTime(task.getStartTime()).equals(startedTask.getStartTime())) {
                startedTask.setEndTime(task.getEndTime());
                getStatistics(workDay, workMonth);
                Ebean.update(workMonth);
                response = Response.status(Response.Status.OK).build();
                return response;
            }
        }
        createTaskAddToWorkDayGetStatisticsAndSave(task, workDay, workMonth);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private Response getContainingMonthAndModifyTaskOrCreateMonthDayAndTask(ModifyTaskRB task, TimeLogger timeLogger) {
        Response response;
        for (WorkMonth workMonth : timeLogger.getMonths()) {
            if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
                return getDayAndModifyItsTaskOrCreateDayAndTask(timeLogger, workMonth, task);
            }
        }
        WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        Task newTask = new Task(task.getNewTaskId());
        setModifiedProperties(newTask, task);
        addMonthDayTaskGetStatisticsAndSave(timeLogger, workMonth, workDay, newTask);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private Response getDayAndModifyItsTaskOrCreateDayAndTask(TimeLogger timeLogger, WorkMonth workMonth, ModifyTaskRB task) {
        Response response;
        for (WorkDay workDay : workMonth.getDays()) {
            if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
                return getTaskToModifyOrCreateNew(timeLogger, workDay, task, workMonth);
            }
        }
        WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
        workMonth.addWorkDay(workDay);
        Task newTask = new Task(task.getNewTaskId());
        setModifiedProperties(newTask, task);
        workDay.addTask(newTask);
        getStatistics(workDay, workMonth);
        Ebean.save(workMonth);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private Response getTaskToModifyOrCreateNew(TimeLogger timeLogger, WorkDay workDay, ModifyTaskRB task, WorkMonth workMonth) {
        Response response;
        for (Task existingTask : workDay.getTasks()) {
            if (task.getTaskId().equals(existingTask.getTaskId())
                    && Task.stringToLocalTime(task.getStartTime()).equals(existingTask.getStartTime())) {
                int matchingIndex = workDay.getTasks().indexOf(existingTask);
                WorkDay testWorkDay = new WorkDay();
                for (int i = 0; i < workDay.getTasks().size(); i++) {
                    if (i != matchingIndex) {
                        testWorkDay.addTask(workDay.getTasks().get(i));
                    }
                }
                existingTask.setTaskId(task.getNewTaskId());
                existingTask = setModifiedProperties(existingTask, task);
                if (!testWorkDay.isSeparatedTime(existingTask)) {
                    response = Response.status(Response.Status.CONFLICT).build();
                } else {
                    getStatistics(workDay, workMonth);
                    Ebean.update(workMonth);
                    response = Response.status(Response.Status.OK).build();
                }
                return response;
            }
        }
        Task newTask = new Task(task.getNewTaskId());
        setModifiedProperties(newTask, task);
        workDay.addTask(newTask);
        getStatistics(workDay, workMonth);
        Ebean.save(workMonth);
        response = Response.status(Response.Status.OK).build();
        return response;
    }

    private TimeLogger getTimeLogger(String token) throws NotAuthorizedException, UnsupportedEncodingException, JoseException {

        if (token != null) {
            String jwt = token.split(" ")[1];
            for (TimeLogger timeLogger : Ebean.find(TimeLogger.class).findList()) {
                JwtClaims existingClaims = new JwtClaims();
                JsonWebSignature existingJws = new JsonWebSignature();
                String existingJwt;
                existingClaims.setSubject(timeLogger.getName());
                existingJws.setPayload(existingClaims.toJson());
                existingJws.setKey(new HmacKey(timeLogger.getPassword().getBytes("UTF-8")));
                existingJws.setKeyIdHeaderValue("kid");
                existingJws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
                existingJws.setDoKeyValidation(false);
                existingJwt = existingJws.getCompactSerialization();
                if (jwt.equals(existingJwt)) {
                    return timeLogger;
                }
            }
            throw new NotAuthorizedException("Not existing user");
        } else {
            throw new NotAuthorizedException("Not existing user");
        }
    }
}
