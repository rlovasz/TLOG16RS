package com.rozsalovasz.tlog16rs.resources;

import com.avaje.ebean.Ebean;
import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.UserRB;
import com.rozsalovasz.tlog16rs.beans.WorkDayRB;
import com.rozsalovasz.tlog16rs.beans.WorkMonthRB;
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
import com.rozsalovasz.tlog16rs.entities.Task;
import com.rozsalovasz.tlog16rs.entities.User;
import com.rozsalovasz.tlog16rs.entities.WorkDay;
import com.rozsalovasz.tlog16rs.resources.services.JwtService;
import com.rozsalovasz.tlog16rs.resources.services.TLOG16RSService;
import io.dropwizard.auth.AuthenticationException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
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
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

/**
 * This is the class where the REST endpoints are written
 *
 * @author rlovasz
 */
@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

    private final TLOG16RSService service = new TLOG16RSService();
    private final JwtService jwtService = new JwtService();

    /**
     * This is a POST method, and it does the logging in.
     *
     * @param user UserRb type which contains a name and a password
     * @return In the returned response it sends the jwt token
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(UserRB user) {
        try {
            String jwt;
            jwt = service.loginUser(user);
            return Response.status(Response.Status.OK).header("Authorization", "Bearer " + jwt).header("Access-Control-Expose-Headers", "Authorization").build();
        } catch (UnsupportedEncodingException | AuthenticationException | JoseException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This GET method refreshes the JWT token.
     *
     * @param token
     * @return
     */
    @GET
    @Path("/refresh-token")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(@HeaderParam("Authorization") String token) {
        try {
            User user = service.getUser(token);
            String jwt = jwtService.generateJwtToken(user);
            return Response.status(Response.Status.OK).header("Authorization", "Bearer " + jwt).header("Access-Control-Expose-Headers", "Authorization").build();
        } catch (UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    /**
     * This POST method registers a new user.
     *
     * @param user UserRB type with name and password. Instead of the given
     * password this method saves into database a decoded value.
     * @return
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserRB user) {
        try {
            Response response;
            User existingUser = Ebean.find(User.class).where().eq("name", user.getName()).findUnique();
            if (existingUser != null) {
                response = Response.status(Response.Status.CONFLICT).build();
            } else {
                service.registerUser(user);
                response = Response.status(Response.Status.OK).build();
            }
            return response;
        } catch (Exception e) {
            log.error("Failed to register user", e);
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
            User user = service.getUser(token);
            return Response.ok(service.addNewMonthToTimeLogger(user, month.getYear(), month.getMonth())).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NotNewMonthException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
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
            User user = service.getUser(token);
            return Response.ok(user.getMonths()).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException ex) {
            log.error(ex.getMessage(), ex);
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
        Ebean.deleteAll(Ebean.find(User.class).findList());
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
            User user = service.getUser(token);
            List<WorkDay> days = service.listSpecificMonth(user, year, month);
            return Response.ok(days).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NotNewMonthException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).build();
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
    public Response addNewDayWeekDay(WorkDayRB day, @HeaderParam("Authorization") String token) {
        try {
            User user = service.getUser(token);
            service.addNewDay(user, day, false);
            return Response.status(Response.Status.OK).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(449).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
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
            User user = service.getUser(token);
            service.addNewDay(user, day, true);
            return Response.status(Response.Status.OK).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage());
            return Response.status(449).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
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
            User user = service.getUser(token);
            List<Task> tasks = service.listSpecificDay(user, year, month, day);
            return Response.ok(tasks).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(449).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
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
     * @throws java.text.ParseException
     */
    @POST
    @Path("/workmonths/workdays/tasks/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startNewTask(StartTaskRB task, @HeaderParam("Authorization") String token) throws ParseException {
        try {
            User user = service.getUser(token);
            service.startNewTask(user, task);
            return Response.status(Response.Status.OK).build();
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NoTaskIdException | EmptyTimeFieldException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.LENGTH_REQUIRED).build();
        } catch (NotExpectedTimeOrderException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(449).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).build();
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
     * @throws java.text.ParseException
     */
    @PUT
    @Path("/workmonths/workdays/tasks/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response finishStartedTask(FinishingTaskRB task, @HeaderParam("Authorization") String token) throws ParseException {
        try {
            User user = service.getUser(token);
            service.finishStartedTask(user, task);
            return Response.status(Response.Status.OK).build();
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(449).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NoTaskIdException | EmptyTimeFieldException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.LENGTH_REQUIRED).build();
        } catch (NotExpectedTimeOrderException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).build();
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
     * @throws java.text.ParseException
     */
    @PUT
    @Path("/workmonths/workdays/tasks/modify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyExistingTask(ModifyTaskRB task, @HeaderParam("Authorization") String token) throws ParseException {
        try {
            User user = service.getUser(token);
            service.modifyExistingTask(user, task);
            return Response.status(Response.Status.OK).build();
        } catch (InvalidTaskIdException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NegativeMinutesOfWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(449).build();
        } catch (FutureWorkException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NoTaskIdException | EmptyTimeFieldException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.LENGTH_REQUIRED).build();
        } catch (NotExpectedTimeOrderException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        } catch (NotNewMonthException | NotNewDateException | NotTheSameMonthException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (WeekendNotEnabledException e) {
            log.error(e.getMessage(), e);
            return Response.status(428).build();
        } catch (NotSeparatedTaskTimesException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    /**
     * This is a PUT method to delete an existing Task.
     *
     * @param task This is a DeleteTaskRB type object, which has the following
     * required fields: int year, int month, int day, String taskId, String
     * startTime
     * @param token
     * @return
     * @throws java.text.ParseException
     */
    @PUT
    @Path("/workmonths/workdays/tasks/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTask(DeleteTaskRB task, @HeaderParam("Authorization") String token) throws ParseException {
        try {
            User user = service.getUser(token);
            service.deleteTask(user, task);
            return Response.status(Response.Status.OK).build();
        } catch (NotAuthorizedException | UnsupportedEncodingException | JoseException | InvalidJwtException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
