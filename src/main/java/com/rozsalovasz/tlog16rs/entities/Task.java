package com.rozsalovasz.tlog16rs.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.core.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.core.NoTaskIdException;
import com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException;
import com.rozsalovasz.tlog16rs.core.NotValidTimeExpressionException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * With the instantiation of this class we can create tasks. We can set a tasks Id, the time of its start, the time of its end, we can add a comment to detail. We can check if a task Id is valid and we can ask for the duration of the task.
 *
 * @author rlovasz
 */
@Getter
@Entity
public class Task {

	@Id
	@GeneratedValue
	int id;

	private String taskId;
	@JsonSerialize(using = LocalTimeSerializer.class)
	private LocalTime startTime;
	@JsonSerialize(using = LocalTimeSerializer.class)
	private LocalTime endTime;
	@Setter
	private String comment = "";
	private long minPerTask;

	/**
	 * @param taskId This is the Id of the task. Redmine project: 4 digits, LT project: LT-(4 digits)
	 * @param comment In this parameter you can add some detail about what did you do exactly.
	 * @param startHour, the hour part of the beginning time
	 * @param startMin, the mint part of the beginning time
	 * @param endHour, the hour part of the finishing time
	 * @param endMin , the min part of the finishing time
	 */
	public Task(String taskId, String comment, int startHour, int startMin, int endHour, int endMin) {
		LocalTime localStartTime = LocalTime.of(startHour, startMin);
		LocalTime localEndTime = LocalTime.of(endHour, endMin);

		this.taskId = taskId;
		this.startTime = localStartTime;
		this.endTime = localEndTime;
		this.comment = comment;
		if (!isValidTaskID()) {
			throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
		}
	}

	/**
	 *
	 * @param taskId This is the Id of the task. Redmine project: 4 digits, LT project: LT-(4 digits)
	 */
	public Task(String taskId) {
		this.taskId = taskId;
		if (!isValidTaskID()) {
			throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
		}
	}

	/**
	 *
	 * @param taskId This is the Id of the task. Redmine project: 4 digits, LT project: LT-(4 digits)
	 * @param comment In this parameter you can add some detail about what did you do exactly.
	 * @param startTimeString the beginning time of task with string in format HH:MM
	 * @param endTimeString the finishing time of task with string in format HH:MM
	 */
	public Task(String taskId, String comment, String startTimeString, String endTimeString) {
		LocalTime localStartTime = stringToLocalTime(startTimeString);
		LocalTime localEndTime = stringToLocalTime(endTimeString);

		this.taskId = taskId;
		this.startTime = localStartTime;
		this.endTime = localEndTime;
		this.comment = comment;
		if (!isValidTaskID()) {
			throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
		}
	}

	/**
	 * @return with the value of comment, but if it is not set, it returns with an empty String
	 */
	public String getComment() {
		if (comment == null) {
			comment = "";
		}

		return comment;
	}

	/**
	 * This method is a getter for the minPerTask field.
	 *
	 * @return with the time interval between startTime and endTime in minutes
	 */
	public long getMinPerTask() {
		if (startTime == null || endTime == null) {
			throw new EmptyTimeFieldException("You leaved out a time argument, you should set it.");
		} else if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
			minPerTask = Duration.between(startTime, endTime).toMinutes();
			return minPerTask;
		} else {
			throw new NotExpectedTimeOrderException("Something went wrong. You should begin"
					+ " your task before you finish it.");
		}

	}

	/**
	 *
	 * @return with the startTime
	 */
	public LocalTime getStartTime() {
		return startTime;
	}

	/**
	 *
	 * @return with the endTime
	 */
	public LocalTime getEndTime() {
		return endTime;
	}

	/**
	 *
	 * @param hour the value of hour with integer
	 * @param min the value of minutes with integer
	 */
	public void setStartTime(int hour, int min) {
		startTime = LocalTime.of(hour, min);
		if (endTime != null) {
			if (!isMultipleQuarterHour()) {
				throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
						+ "Please reconsider the time interval");
			}
		}
	}

	/**
	 *
	 * @param hour the value of hour with integer
	 * @param min the value of minutes with integer
	 */
	public void setEndTime(int hour, int min) {
		endTime = LocalTime.of(hour, min);
		if (startTime != null) {
			if (!isMultipleQuarterHour()) {
				throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
						+ "Please reconsider the time interval");
			}
		}
	}

	/**
	 *
	 * @param time The String value of time in format HH:MM
	 */
	public void setStartTime(String time) {
		startTime = stringToLocalTime(time);
		if (endTime != null) {
			if (!isMultipleQuarterHour()) {
				throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
						+ "Please reconsider the time interval");
			}
		}
	}

	/**
	 *
	 * @param time The String value of time in format HH:MM
	 */
	public void setEndTime(String time) {
		endTime = stringToLocalTime(time);
		if (startTime != null) {
			if (!isMultipleQuarterHour()) {
				throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
						+ "Please reconsider the time interval");
			}
		}
	}

	/**
	 * @param taskId The parameter to set
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
		if (!isValidTaskID()) {
			throw new InvalidTaskIdException("It is not a valid task Id. Valid id's: 4 digits or LT-4 digits");
		}
	}

	/**
	 * This method creates a LocalTime variable by transforming the String
	 *
	 * @param time The String value of time in format HH:MM
	 * @return
	 */
	public static LocalTime stringToLocalTime(String time) {
		if (time != null) {
			String hourString = time.split(":")[0];
			String minString = time.split(":")[1];
			int hour = Integer.parseUnsignedInt(hourString);
			int min = Integer.parseUnsignedInt(minString);
			return LocalTime.of(hour, min);
		} else {
			throw new EmptyTimeFieldException("You leaved out a time argument, you should set it.");
		}
	}

	/**
	 * This method checks if the Id of the task is a valid redmine task Id.
	 *
	 * @return true, if it is valid, false if it isn't valid.
	 */
	private boolean isValidRedmineTaskId() {

		return taskId.matches("\\d{4}");

	}

	/**
	 * This method checks if the Id of the task is a valid LT task Id.
	 *
	 * @return true, if it is valid, false if it isn't valid.
	 */
	private boolean isValidLTTaskId() {

		return taskId.matches("LT-\\d{4}");

	}

	/**
	 * This method checks if the Id of the task is a valid task Id (redmine or LT project task id).
	 *
	 * @return true, if it is valid, false if it isn't valid.
	 */
	private boolean isValidTaskID() {

		if (taskId == null) {
			throw new NoTaskIdException("There is no task Id, please set a valid Id!");
		} else {
			return isValidLTTaskId() || isValidRedmineTaskId();
		}
	}

	/**
	 * This method checks if the minutes are the multiple of quarter hour
	 *
	 * @return true, if it is multiple, but false if it isn't.
	 */
	protected boolean isMultipleQuarterHour() {
		return getMinPerTask() % 15 == 0;

	}

	/**
	 *
	 * @return with the String representation of the Task type
	 */
	@Override
	public String toString() {
		return "Task Id: " + taskId + ", Start time: " + startTime + ", End Time: " + endTime + ", Comment: " + comment;
	}

	private static class LocalTimeSerializer extends JsonSerializer<LocalTime> {

		@Override
		public void serialize(LocalTime t, JsonGenerator jg, SerializerProvider sp) throws IOException {
			jg.writeString(t.toString());
		}
	}

}
