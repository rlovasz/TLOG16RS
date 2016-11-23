package com.rozsalovasz.tlog16rs.resources;

import com.avaje.ebean.Ebean;
import com.rozsalovasz.tlog16rs.beans.DeleteTaskRB;
import com.rozsalovasz.tlog16rs.beans.FinishingTaskRB;
import com.rozsalovasz.tlog16rs.beans.ModifyTaskRB;
import com.rozsalovasz.tlog16rs.beans.StartTaskRB;
import com.rozsalovasz.tlog16rs.beans.WorkDayRB;
import com.rozsalovasz.tlog16rs.beans.WorkMonthRB;
import com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException;
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
import java.time.LocalDate;
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
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the class where the REST endpoint are written
 *
 * @author rlovasz
 */
@Path("/timelogger")
@Slf4j
public class TLOG16RSResource {

	/**
	 * This is a POST method to add a new working month to the database
	 *
	 * @param month is a RestBean, where are the important data to create the new WorkMonth (year, month)
	 * @return with the created WorkMonth object
	 */
	@POST
	@Path("/workmonths")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public WorkMonth addNewMonth(WorkMonthRB month) {
		try {
			TimeLogger timeLogger = getTimeLogger();
			return addNewMonthAndSaveTimeLogger(month.getYear(), month.getMonth(), timeLogger);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new WorkMonth(1970, 1);
		}
	}

	/**
	 * This is a GET method to list all the existing months of the database
	 *
	 * @return the list of WorkMonth objects
	 */
	@GET
	@Path("/workmonths")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WorkMonth> listWorkMonths() {
		try {
			return Ebean.find(WorkMonth.class).findList();
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * This is a PUT method to delete the whole database
	 */
	@PUT
	@Path("/workmonths/deleteall")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteAllMonths() {
		try {
			int i = 1;
			while (!Ebean.find(TimeLogger.class).findList().isEmpty()) {
				Ebean.delete(TimeLogger.class, i);
				i++;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * This is a GET method, which displays the list of WorkDays in the month which is specifies in the path
	 *
	 * @param year
	 * @param month
	 * @return with the list of WorkDay objects
	 */
	@GET
	@Path("/workmonths/{year}/{month}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<WorkDay> listSpecificMonth(@PathParam("year") int year, @PathParam("month") int month) {
		try {
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(year, month).toString())) {
					return workMonth.getDays();
				}
			}
			TimeLogger timeLogger = getTimeLogger();
			WorkMonth workMonth = addNewMonthAndSaveTimeLogger(year, month, timeLogger);
			return workMonth.getDays();
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * This is a POST method, which saves a new work day into the database, which is specified in the WorkDayRB type parameter. If the work month is not created yet, it creates it.
	 *
	 * @param day Specifies the day to create with the following data: year, month, day, requiredHours
	 * @return with the created WorkDay object
	 */
	@POST
	@Path("/workmonths/workdays")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewDay(WorkDayRB day) {
		Response response;
		try {
			WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(workDay.getActualDay().getYear(), workDay.getActualDay().getMonthValue()).toString())) {
					addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay);
					response = Response.status(Response.Status.OK).build();
					return response;
				}
			}
			TimeLogger timeLogger = getTimeLogger();
			addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
			response = Response.status(Response.Status.OK).build();
			return response;
		} catch (WeekendNotEnabledException e) {
			log.error(e.getMessage());
			response = Response.status(428).build();
			return response;
		} catch (FutureWorkException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.FORBIDDEN).build();
			return response;
		} catch (NegativeMinutesOfWorkException e) {
			log.error(e.getMessage());
			response = Response.status(449).build();//Retry with status code
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}
	}

	/**
	 * This is a POST method, which saves a new work day on the weekend into the database, which is specified in the WorkDayRB type parameter. If the work month is not created yet, it creates it.
	 *
	 * @param day Specifies the weekend day to create with the following data: year, month, day, requiredHours
	 * @return with the created WorkDay object
	 */
	@POST
	@Path("/workmonths/workdays/weekend")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewDayWeekend(WorkDayRB day) {
		Response response;
		try {
			WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(workDay.getActualDay().getYear(), workDay.getActualDay().getMonthValue()).toString())) {
					addWeekendWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay);
					response = Response.status(Response.Status.OK).build();
					return response;
				}
			}
			TimeLogger timeLogger = getTimeLogger();
			addNewMonthAndWeekendDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
			response = Response.status(Response.Status.OK).build();
			return response;
		} catch (FutureWorkException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.FORBIDDEN).build();
			return response;
		} catch (NegativeMinutesOfWorkException e) {
			log.error(e.getMessage());
			response = Response.status(449).build();//Retry with status code
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}
	}

	@PUT
	@Path("/workmonths/workdays/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response modifyRequiredMinPerDay(WorkDayRB day) {
		Response response;
		try {
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(day.getYear(), day.getMonth()).toString())) {
					for (WorkDay workDay : Ebean.find(WorkDay.class).findList()) {
						if (workDay.getActualDay().equals(LocalDate.of(day.getYear(), day.getMonth(), day.getDay()))) {
							TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
							workDay.setRequiredMinPerDay((int) (day.getRequiredHours() * 60));
							getStatistics(workDay, workMonth);
							Ebean.save(workDay);
							Ebean.update(timeLogger);
							response = Response.status(Response.Status.OK).build();
							return response;
						}

					}
					TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
					WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
					workMonth.addWorkDay(workDay);
					getStatistics(workDay, workMonth);
					saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
					response = Response.status(Response.Status.OK).build();
					return response;
				}
			}
			TimeLogger timeLogger = getTimeLogger();
			WorkDay workDay = new WorkDay((int) ((day.getRequiredHours()) * 60), day.getYear(), day.getMonth(), day.getDay());
			addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(day.getYear(), day.getMonth(), timeLogger, workDay);
			response = Response.status(Response.Status.OK).build();
			return response;
		} catch (NegativeMinutesOfWorkException e) {
			log.error(e.getMessage());
			response = Response.status(449).build();//Retry with status code
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}
	}

	/**
	 * This is a GET method, which displays the list of tasks of the day specified by the parameters in the path
	 *
	 * @param year
	 * @param month
	 * @param day
	 * @return with the list of Task objects
	 */
	@GET
	@Path("/workmonths/{year}/{month}/{day}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> listSpecificDay(@PathParam("year") int year, @PathParam("month") int month, @PathParam("day") int day) {
		try {
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(year, month).toString())) {
					return getTasksOfSpecifiedDayOrEmptyListIfNotExist(workMonth, day, year, month);
				}
			}
			TimeLogger timeLogger = getTimeLogger();
			WorkDay workDay = new WorkDay(year, month, day);
			addNewMonthAndDayGetStaticsticsAndSaveTimeLogger(year, month, timeLogger, workDay);
			return workDay.getTasks();
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * This is a POST method, which creates a new Task in the database, where the endTime is not known yet. If the day or the month is not created yet, then it creates them.
	 *
	 * @param task This is a StartTaskRB type object, it has the following required fields: int year, int month, int day, String taskId, String comment, String startTime
	 * @return with the Task object
	 */
	@POST
	@Path("/workmonths/workdays/tasks/start")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startNewTask(StartTaskRB task) {
		Response response;
		try {
			Task startedTask = new Task(task.getTaskId());
			startedTask.setComment(task.getComment());
			startedTask.setStartTime(task.getStartTime());
			startedTask.setEndTime(task.getStartTime());
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (isTheSpecifiedDayExistThenAddTheTask(workMonth, task, startedTask)
						&& workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
					response = Response.status(Response.Status.OK).build();
					return response;
				} else if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
					createTheDayThenAddTheTask(task, workMonth, startedTask);
					response = Response.status(Response.Status.OK).build();
					return response;
				}
			}
			createMonthAndDayThenAddTask(task, startedTask);
			response = Response.status(Response.Status.OK).build();
			return response;
		} catch (InvalidTaskIdException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
			return response;
		} catch (NotSeparatedTaskTimesException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.CONFLICT).build();
			return response;
		}catch (EmptyTimeFieldException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}

	}

	/**
	 * This is a PUT object, which finishes a started Task with the information of endTime. If the month, the day or the task doesn't exist yet, it created them with the given informations.
	 *
	 * @param task it is a FinishingTaskRB object, it has the following required fields: int year, int month, int day, String taskId, String startTime, String endTime
	 * @return with the finished Task object
	 */
	@PUT
	@Path("/workmonths/workdays/tasks/finish")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response finishStartedTask(FinishingTaskRB task) {
		Response response;
		try {
			return getTheContainingMonthAndDayAndFinishTaskOrCreateMonthDayAndTask(task);
		} catch (InvalidTaskIdException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
			return response;
		} catch (NotSeparatedTaskTimesException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.CONFLICT).build();
			return response;
		} catch (NotExpectedTimeOrderException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.EXPECTATION_FAILED).build();
			return response;
		} catch (NotMultipleQuarterHourException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}

	}

	/**
	 * This is a PUT method to modify any property of the specified Task. If the month, the day or the task doesn't exist, it will create them with the given informations.
	 *
	 * @param task This is a ModifyTaskRB type object, which has the following required fields: int year, int month, int day, String taskId, String startTime, String newTaskId, String newComment, String newStartTime, String newEndTime
	 * @return with the modified Task object
	 */
	@PUT
	@Path("/workmonths/workdays/tasks/modify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response modifyExistingTask(ModifyTaskRB task) {
		Response response;
		try {
			return getContainingMonthAndModifyTaskOrCreateMonthDayAndTask(task);
		} catch (InvalidTaskIdException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.NOT_ACCEPTABLE).build();
			return response;
		} catch (NotSeparatedTaskTimesException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.CONFLICT).build();
			return response;
		} catch (NotExpectedTimeOrderException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.EXPECTATION_FAILED).build();
			return response;
		} catch (NotMultipleQuarterHourException e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			response = Response.status(Response.Status.BAD_REQUEST).build();
			return response;
		}
	}

	/**
	 * This is a PUT method to delete an existing Task.
	 *
	 * @param task This is a DeleteTaskRB type object, which has the following required fields: int year, int month, int day, String taskId, String startTime
	 */
	@PUT
	@Path("/workmonths/workdays/tasks/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteTask(DeleteTaskRB task
	) {
		try {
			for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
				if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
					getDayAndDeleteItsSpecifiedTask(workMonth, task);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	private void getDayAndDeleteItsSpecifiedTask(WorkMonth workMonth, DeleteTaskRB task) {
		for (WorkDay workDay : workMonth.getDays()) {
			if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
				getSpecifiedTaskEndDeleteIt(workDay, task);
			}
		}
	}

	private void getSpecifiedTaskEndDeleteIt(WorkDay workDay, DeleteTaskRB task) {
		for (int i = 0; i < workDay.getTasks().size(); i++) {
			if (task.getTaskId().equals(workDay.getTasks().get(i).getTaskId())
					&& Task.stringToLocalTime(task.getStartTime()).equals(workDay.getTasks().get(i).getStartTime())) {
				Ebean.delete(Task.class, i + 1);
				Ebean.delete(workDay.getTasks().get(i));
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

	private void addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(WorkMonth workMonth, WorkDay workDay) {
		TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
		workMonth.addWorkDay(workDay);
		getStatistics(workDay, workMonth);
		saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
	}

	private void addWeekendWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(WorkMonth workMonth, WorkDay workDay) {
		TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
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

	private void setModifiedProperties(Task existingTask, ModifyTaskRB task) {
		existingTask.setComment(task.getNewComment());
		existingTask.setStartTime(task.getNewStartTime());
		existingTask.setEndTime(task.getNewEndTime());
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

	private void createMonthAndDayThenAddTask(StartTaskRB task, Task startedTask) {
		TimeLogger timeLogger = getTimeLogger();
		WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
		WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
		addMonthDayTaskGetStatisticsAndSave(timeLogger, workMonth, workDay, startedTask);
	}

	private void createTheDayThenAddTheTask(StartTaskRB task, WorkMonth workMonth, Task startedTask) {
		TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
		WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
		workMonth.addWorkDay(workDay);
		workDay.addTask(startedTask);
		getStatistics(workDay, workMonth);
		saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
	}

	private boolean isTheSpecifiedDayExistThenAddTheTask(WorkMonth workMonth, StartTaskRB task, Task startedTask) {
		for (WorkDay workDay : workMonth.getDays()) {
			if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
				TimeLogger timeLogger = Ebean.find(TimeLogger.class).findUnique();
				workDay.addTask(startedTask);
				getStatistics(workDay, workMonth);
				saveWorkMonthUpdateTimeLogger(workMonth, timeLogger);
				return true;
			}
		}
		return false;
	}

	private List<Task> getTasksOfSpecifiedDayOrEmptyListIfNotExist(WorkMonth workMonth, int day, int year, int month) {
		for (WorkDay workDay : workMonth.getDays()) {
			if (workDay.getActualDay().getDayOfMonth() == day) {
				return workDay.getTasks();
			}
		}
		WorkDay workDay = new WorkDay(year, month, day);
		addWorkDayToExistingTimeLoggerGetStatisticsAndUpdate(workMonth, workDay);
		return workDay.getTasks();
	}

	private Response getTheContainingMonthAndDayAndFinishTaskOrCreateMonthDayAndTask(FinishingTaskRB task) {
		Response response;
		for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
			if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
				return getSpecifiedDayAndFinishTheTaskOrCreateDayAndTask(workMonth, task);
			}
		}
		TimeLogger timeLogger = getTimeLogger();
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

	private Response getContainingMonthAndModifyTaskOrCreateMonthDayAndTask(ModifyTaskRB task) {
		Response response;
		for (WorkMonth workMonth : Ebean.find(WorkMonth.class).findList()) {
			if (workMonth.getMonthDate().equals(YearMonth.of(task.getYear(), task.getMonth()).toString())) {
				return getDayAndModifyItsTaskOrCreateDayAndTask(workMonth, task);
			}
		}
		TimeLogger timeLogger = getTimeLogger();
		WorkMonth workMonth = new WorkMonth(task.getYear(), task.getMonth());
		WorkDay workDay = new WorkDay(task.getYear(), task.getMonth(), task.getDay());
		Task newTask = new Task(task.getNewTaskId());
		setModifiedProperties(newTask, task);
		addMonthDayTaskGetStatisticsAndSave(timeLogger, workMonth, workDay, newTask);
		response = Response.status(Response.Status.OK).build();
		return response;
	}

	private Response getDayAndModifyItsTaskOrCreateDayAndTask(WorkMonth workMonth, ModifyTaskRB task) {
		Response response;
		for (WorkDay workDay : workMonth.getDays()) {
			if (workDay.getActualDay().getDayOfMonth() == task.getDay()) {
				return getTaskToModifyOrCreateNew(workDay, task, workMonth);
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

	private Response getTaskToModifyOrCreateNew(WorkDay workDay, ModifyTaskRB task, WorkMonth workMonth) {
		Response response;
		for (Task existingTask : workDay.getTasks()) {
			if (task.getTaskId().equals(existingTask.getTaskId())
					&& Task.stringToLocalTime(task.getStartTime()).equals(existingTask.getStartTime())) {
				int matchingIndex = workDay.getTasks().indexOf(existingTask);
				WorkDay testWorkDay = new WorkDay();
				for(int i=0; i<workDay.getTasks().size(); i++){
					if(i != matchingIndex){
						testWorkDay.addTask(workDay.getTasks().get(i));
					}
				}
				existingTask.setTaskId(task.getNewTaskId());
				setModifiedProperties(existingTask, task);
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

	private TimeLogger getTimeLogger() {
		TimeLogger timeLogger;
		if (Ebean.find(TimeLogger.class).findList().isEmpty()) {
			timeLogger = new TimeLogger("Lovász Rózsa");
		} else {
			timeLogger = Ebean.find(TimeLogger.class).findUnique();
		}
		return timeLogger;
	}

}
