package com.rozsalovasz.tlog16rs.entities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rozsalovasz.tlog16rs.core.FutureWorkException;
import com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException;
import com.rozsalovasz.tlog16rs.core.NotSeparatedTaskTimesException;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;

/**
 * With the instantiation of this class we can create work days. We can set the date of the work day, and the required minutes we should work today. We can check if the work day is weekday, and we can add tasks for the day. We can ask for the sum of the working minutes on this work day, and the extra minutes.
 *
 * @author rlovasz
 */
@Getter
@Entity
public class WorkDay {

	@Id
	@GeneratedValue
	int id;

	private static final List<DayOfWeek> WEEKDAYS = Arrays.asList(
			DayOfWeek.MONDAY,
			DayOfWeek.TUESDAY,
			DayOfWeek.WEDNESDAY,
			DayOfWeek.THURSDAY,
			DayOfWeek.FRIDAY
	);
	private static final int DEFAULT_REQUIRED_MIN_PER_DAY = (int) (7.5 * 60);
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Task> tasks = new ArrayList<>();
	private long requiredMinPerDay;
	private long extraMinPerDay;
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate actualDay;
	private long sumPerDay = 0;

	/**
	 * @param requiredMinPerDay In this parameter you can set the minutes you should work today.
	 * @param year
	 * @param month
	 * @param day
	 */
	public WorkDay(long requiredMinPerDay, int year, int month, int day) {
		LocalDate localActualDay = LocalDate.of(year, month, day);
		if (requiredMinPerDay <= 0) {
			throw new NegativeMinutesOfWorkException("You set a negative value for required minutes, you should set a non-negative value!");
		}
		if (localActualDay.isAfter(LocalDate.now())) {
			throw new FutureWorkException("You cannot work later than today, you should set an other day!");
		}
		this.requiredMinPerDay = requiredMinPerDay;
		this.actualDay = localActualDay;
	}

	/**
	 * The default actual day will be today (server time).
	 *
	 * @param requiredMinPerDay In this parameter you can set the minutes you should work today.
	 */
	public WorkDay(long requiredMinPerDay) {
		this(requiredMinPerDay, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
	}

	/**
	 * The default required minutes will be 450 min=7.5 h
	 *
	 * @param year, the year value of the date in YYYY format
	 * @param month, the month value of the date with simple integer value
	 * @param day, the day value of the date with simple integer value
	 */
	public WorkDay(int year, int month, int day) {
		this(DEFAULT_REQUIRED_MIN_PER_DAY, year, month, day);
	}

	/**
	 * The default actual day will be today (server time), the default required minutes will be 450 min = 7.5 h
	 */
	public WorkDay() {
		this(DEFAULT_REQUIRED_MIN_PER_DAY, LocalDate.now().getYear(), LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth());
	}

	/**
	 * We can set the amount of the minutes the employee should work this day.
	 *
	 * @param requiredMinPerDay the value which will be set
	 */
	public void setRequiredMinPerDay(long requiredMinPerDay) {
		if (requiredMinPerDay <= 0) {
			throw new NegativeMinutesOfWorkException("You set a negative value for required minutes, you should set a non-negative value!");
		}
		this.requiredMinPerDay = requiredMinPerDay;
	}

	/**
	 * We can set the date of the actual day.
	 *
	 * @param year, the year value of the date in YYYY format
	 * @param month, the month value of the date with simple integer value
	 * @param day, the day value of the date with simple integer value
	 */
	public void setActualDay(int year, int month, int day) {
		LocalDate localActualDay = LocalDate.of(year, month, day);
		if (localActualDay.isAfter(LocalDate.now())) {
			throw new FutureWorkException("You cannot work later than today, you should set an other day!");
		}
		this.actualDay = localActualDay;
	}

	/**
	 * This method calculates the difference between the minutes while the employee worked and the minutes while the employee should have worked
	 *
	 * @return with the signed value of the extra minutes on this work day. If it is positive the employee worked more, if it is negative the employee worked less, then the required.
	 */
	public long getExtraMinPerDay() {
		extraMinPerDay = getSumPerDay() - requiredMinPerDay;
		return extraMinPerDay;
	}

	/**
	 * This methods calculates the sum of the minutes of the tasks of this work day.
	 *
	 * @return with the minutes while the employee worked on this work day
	 */
	public long getSumPerDay() {
		if (sumPerDay == 0) {
			sumPerDay = tasks.stream().mapToLong(Task::getMinPerTask).sum();
		}

		return sumPerDay;
	}

	/**
	 * This method adds a new task to the List named tasks, after it checks if the minutes of the task are the multiple of a quarter hour. If it would be false, this method throws
	 *
	 * @param task It is a Task type parameter, which will be added
	 */
	public void addTask(Task task) {
		if (task.isMultipleQuarterHour() && isSeparatedTime(task)) {
			tasks.add(task);
			sumPerDay = 0;
		} else if (!isSeparatedTime(task)) {
			throw new NotSeparatedTaskTimesException("You should separate the time intervals of your tasks!");
		} else {
			throw new NotMultipleQuarterHourException("The smallest portion of time is 15 minutes. "
					+ "Please reconsider the time interval");
		}
	}

	/**
	 * This method decides if this work day is a weekday or notexpResult
	 *
	 * @return true if it is a weekday, false if it is on weekend
	 */
	protected boolean isWeekday() {
		return WEEKDAYS.contains(DayOfWeek.from(actualDay));
	}

	/**
	 * This method decides, if the task parameter has common time interval with one of the existing tasks
	 *
	 * @param task the parameter to check
	 * @return true, if there is no common time interval, false, if there is a common time interval
	 */
	public boolean isSeparatedTime(Task task) {
		for (Task t : tasks) {
			boolean existingBeginsEarlier = t.getStartTime().isBefore(task.getStartTime()) && task.getStartTime().isBefore(t.getEndTime());
			boolean newBeginsEarlier = t.getStartTime().isAfter(task.getStartTime()) && t.getStartTime().isBefore(task.getEndTime());
			boolean endsOrBeginsTogether = t.getEndTime().equals(task.getEndTime()) || t.getStartTime().equals(task.getStartTime());
			if ((existingBeginsEarlier || newBeginsEarlier || endsOrBeginsTogether) && !tasks.isEmpty()) {
					return false;
				}

		}
		return true;
	}

	private static class LocalDateSerializer extends JsonSerializer<LocalDate> {

		@Override
		public void serialize(LocalDate t, JsonGenerator jg, SerializerProvider sp) throws IOException {
			jg.writeString(t.toString());
		}
	}

}
