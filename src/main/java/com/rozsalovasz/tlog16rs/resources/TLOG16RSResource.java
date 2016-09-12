package com.rozsalovasz.tlog16rs.resources;


import com.rozsalovasz.tlog16rs.core.EmptyTimeFieldException;
import com.rozsalovasz.tlog16rs.core.FutureWorkException;
import com.rozsalovasz.tlog16rs.core.InvalidTaskIdException;
import com.rozsalovasz.tlog16rs.core.NegativeMinutesOfWorkException;
import com.rozsalovasz.tlog16rs.core.NoMonthsException;
import com.rozsalovasz.tlog16rs.core.NoTaskIdException;
import com.rozsalovasz.tlog16rs.core.NotExpectedTimeOrderException;
import com.rozsalovasz.tlog16rs.core.NotMultipleQuarterHourException;
import com.rozsalovasz.tlog16rs.core.NotNewDateException;
import com.rozsalovasz.tlog16rs.core.NotSameYearException;
import com.rozsalovasz.tlog16rs.core.NotSeparatedTaskTimesException;
import com.rozsalovasz.tlog16rs.core.NotTheSameMonthException;
import com.rozsalovasz.tlog16rs.core.Task;
import com.rozsalovasz.tlog16rs.core.TimeLogger;
import com.rozsalovasz.tlog16rs.core.WeekendIsNotEnabledException;
import com.rozsalovasz.tlog16rs.core.WorkDay;
import com.rozsalovasz.tlog16rs.core.WorkMonth;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/timelogger")
@Produces(MediaType.APPLICATION_JSON)
public class TLOG16RSResource {

    public LocalDate getDate(TimeLogger all, String monthAsStirng, String dayAsString) {
        int year = all.getMonths().get(0).getDays().get(0).getActualDay().getYear();
        LocalDate day;
        switch (monthAsStirng) {
            case "august":
                day = LocalDate.of(year, 8, Integer.parseInt(dayAsString));
                break;
            case "september":
                day = LocalDate.of(year, 9, Integer.parseInt(dayAsString));
                break;
            default:
                day = LocalDate.now();
        }
        return day;
    }

    @GET
    @Path("/workmonths/{month}/workdays/{day}/tasks")
    public Task getJsonTaskDefault(@PathParam("month") String month, @PathParam("day") String day) {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400, LocalDate.of(2016, 9, 8));
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
                workDay1.addTask(task1);
                workDay1.addTask(task2);
                workDay2.addTask(task3);
                workDay2.addTask(task4);
                workDay3.addTask(task5);
                workDay3.addTask(task6);
                workDay4.addTask(task7);
                workDay4.addTask(task8);
                september.addWorkDay(workDay1);
                september.addWorkDay(workDay2);
                august.addWorkDay(workDay3);
                august.addWorkDay(workDay4);
                all.addMonth(august);
                all.addMonth(september);
            LocalDate date = getDate(all, month, day);
            for (WorkMonth workMonth : all.getMonths()) {
                if (workMonth.getDays().get(0).getActualDay().getMonth().equals(date.getMonth())) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().equals(date)) {
                            return workDay.getTasks().get(0);
                        }

                    }
                }
            }
            return new Task();

        } catch (InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException 
                | FutureWorkException | NotMultipleQuarterHourException | NotExpectedTimeOrderException 
                | EmptyTimeFieldException | NotSeparatedTaskTimesException | WeekendIsNotEnabledException 
                | NotNewDateException | NotTheSameMonthException | NotSameYearException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new Task();
        }
    }

    @GET
    @Path("/workmonths/{month}/workdays/{day}/tasks/{i}")
    public Task getJsonTask(@PathParam("month") String month, @PathParam("day") String day, @PathParam("i") int i) {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400, LocalDate.of(2016, 9, 8));
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
                workDay1.addTask(task1);
                workDay1.addTask(task2);
                workDay2.addTask(task3);
                workDay2.addTask(task4);
                workDay3.addTask(task5);
                workDay3.addTask(task6);
                workDay4.addTask(task7);
                workDay4.addTask(task8);
                september.addWorkDay(workDay1);
                september.addWorkDay(workDay2);
                august.addWorkDay(workDay3);
                august.addWorkDay(workDay4);
                all.addMonth(august);
                all.addMonth(september);
            LocalDate date = getDate(all, month, day);
            for (WorkMonth workMonth : all.getMonths()) {
                if (workMonth.getDays().get(0).getActualDay().getMonth().equals(date.getMonth())) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().equals(date)) {
                            return workDay.getTasks().get(i - 1);
                        }

                    }
                }
            }
            return new Task();

        } catch (InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException | FutureWorkException 
                | NotMultipleQuarterHourException | NotExpectedTimeOrderException | EmptyTimeFieldException 
                | NotSeparatedTaskTimesException | WeekendIsNotEnabledException | NotNewDateException 
                | NotTheSameMonthException | NotSameYearException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new Task();
        }
    }

    @GET
    @Path("/workmonths/{month}/workdays")
    public WorkDay getJsonWorkDayDefault(@PathParam("month") String month) throws NegativeMinutesOfWorkException, FutureWorkException {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400, LocalDate.of(2016, 9, 8));
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkDay workDay5 = new WorkDay(LocalDate.of(2016, 9, 1));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
            workDay1.addTask(task1);
            workDay1.addTask(task2);
            workDay2.addTask(task3);
            workDay2.addTask(task4);
            workDay3.addTask(task5);
            workDay3.addTask(task6);
            workDay4.addTask(task7);
            workDay4.addTask(task8);
            workDay5.addTask(task8);
            september.addWorkDay(workDay1);
            september.addWorkDay(workDay2);
            september.addWorkDay(workDay5);
            august.addWorkDay(workDay3);
            august.addWorkDay(workDay4);
            all.addMonth(august);
            all.addMonth(september);
            LocalDate date = getDate(all, month, "1");
            for (WorkMonth workMonth : all.getMonths()) {
                if (workMonth.getDays().get(0).getActualDay().getMonth().equals(date.getMonth())) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().equals(date)) {
                            return workDay;
                        }

                    }
                }
            }
            return new WorkDay();
        } catch (WeekendIsNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSameYearException |
                NotMultipleQuarterHourException | NotExpectedTimeOrderException | EmptyTimeFieldException |
                NotSeparatedTaskTimesException | InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException | FutureWorkException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new WorkDay();
        }

    }

    @GET
    @Path("/workmonths/{month}/workdays/{day}")
    public WorkDay getJsonWorkDay(@PathParam("month") String month, @PathParam("day") String day) throws NegativeMinutesOfWorkException, FutureWorkException {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400, LocalDate.of(2016, 9, 8));
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
            workDay1.addTask(task1);
            workDay1.addTask(task2);
            workDay2.addTask(task3);
            workDay2.addTask(task4);
            workDay3.addTask(task5);
            workDay3.addTask(task6);
            workDay4.addTask(task7);
            workDay4.addTask(task8);
            september.addWorkDay(workDay1);
            september.addWorkDay(workDay2);
            august.addWorkDay(workDay3);
            august.addWorkDay(workDay4);
            all.addMonth(august);
            all.addMonth(september);
            LocalDate date = getDate(all, month, day);
            for (WorkMonth workMonth : all.getMonths()) {
                if (workMonth.getDays().get(0).getActualDay().getMonth().equals(date.getMonth())) {
                    for (WorkDay workDay : workMonth.getDays()) {
                        if (workDay.getActualDay().equals(date)) {
                            return workDay;
                        }

                    }
                }
            }
            return new WorkDay();

        } catch (InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException | FutureWorkException |
                NotMultipleQuarterHourException | NotExpectedTimeOrderException | EmptyTimeFieldException |
                NotSeparatedTaskTimesException | WeekendIsNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSameYearException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new WorkDay();
        }

    }

    @GET
    @Path("/workmonths")
    public WorkMonth getJsonWorkMonthDefault() {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400);
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
            workDay1.addTask(task1);
            workDay1.addTask(task2);
            workDay2.addTask(task3);
            workDay2.addTask(task4);
            workDay3.addTask(task5);
            workDay3.addTask(task6);
            workDay4.addTask(task7);
            workDay4.addTask(task8);
            september.addWorkDay(workDay1);
            september.addWorkDay(workDay2);
            august.addWorkDay(workDay3);
            august.addWorkDay(workDay4);
            all.addMonth(august);
            all.addMonth(september);
            return all.Min();
        } catch (InvalidTaskIdException | NoTaskIdException | NoMonthsException | NegativeMinutesOfWorkException | FutureWorkException | NotMultipleQuarterHourException | NotExpectedTimeOrderException | EmptyTimeFieldException | NotSeparatedTaskTimesException | WeekendIsNotEnabledException |
                NotNewDateException | NotTheSameMonthException | NotSameYearException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new WorkMonth();
        }

    }

    @GET
    @Path("/workmonths/{month}")
    public WorkMonth getJsonWorkMonth(@PathParam("month") String month) {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400);
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            workDay1.addTask(task1);
            workDay1.addTask(task2);
            workDay2.addTask(task3);
            workDay2.addTask(task4);
            workDay3.addTask(task5);
            workDay3.addTask(task6);
            workDay4.addTask(task7);
            workDay4.addTask(task8);
            september.addWorkDay(workDay1);
            september.addWorkDay(workDay2);
            august.addWorkDay(workDay3);
            august.addWorkDay(workDay4);
            switch (month) {
                case "august":
                    return august;
                case "september":
                    return september;
                default:
                    return new WorkMonth();
            }
        } catch (InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException |
                FutureWorkException | NotMultipleQuarterHourException | NotExpectedTimeOrderException |
                EmptyTimeFieldException | NotSeparatedTaskTimesException | WeekendIsNotEnabledException | NotNewDateException | NotTheSameMonthException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new WorkMonth();
        }

    }

    @GET
    public TimeLogger getJsonTimeLogger() {
        try {
            Task task1 = new Task("4585", "This is a comment", LocalTime.of(7, 30), LocalTime.of(8, 45));
            Task task2 = new Task("LT-1854", "This is a new comment", LocalTime.of(8, 45), LocalTime.of(10, 30));
            Task task3 = new Task("1245", "", LocalTime.of(10, 30), LocalTime.of(14, 45));
            Task task4 = new Task("1548", "This is a new day", LocalTime.of(7, 30), LocalTime.of(10, 0));
            Task task5 = new Task("LT-7856", "This is a new day's new comment", LocalTime.of(10, 45), LocalTime.of(12, 30));
            Task task6 = new Task("1568", "Blabla", LocalTime.of(7, 45), LocalTime.of(9, 45));
            Task task7 = new Task("LT-4345", "kfhlkd", LocalTime.of(10, 45), LocalTime.of(13, 30));
            Task task8 = new Task("5665", "gdfgfdg", LocalTime.of(7, 30), LocalTime.of(10, 0));
            WorkDay workDay1 = new WorkDay(LocalDate.of(2016, 9, 7));
            WorkDay workDay2 = new WorkDay(400);
            WorkDay workDay3 = new WorkDay(LocalDate.of(2016, 8, 31));
            WorkDay workDay4 = new WorkDay(LocalDate.of(2016, 8, 30));
            WorkMonth september = new WorkMonth();
            WorkMonth august = new WorkMonth();
            TimeLogger all = new TimeLogger();
            workDay1.addTask(task1);
            workDay1.addTask(task2);
            workDay2.addTask(task3);
            workDay2.addTask(task4);
            workDay3.addTask(task5);
            workDay3.addTask(task6);
            workDay4.addTask(task7);
            workDay4.addTask(task8);
            september.addWorkDay(workDay1);
            september.addWorkDay(workDay2);
            august.addWorkDay(workDay3);
            august.addWorkDay(workDay4);
            all.addMonth(august);
            all.addMonth(september);
            return all;
        } catch (InvalidTaskIdException | NoTaskIdException | NegativeMinutesOfWorkException |
                FutureWorkException | NotMultipleQuarterHourException | NotExpectedTimeOrderException | EmptyTimeFieldException | NotSeparatedTaskTimesException | WeekendIsNotEnabledException | NotNewDateException | NotTheSameMonthException | NotSameYearException ex) {
            Logger.getLogger(TLOG16RSResource.class.getName()).log(Level.SEVERE, null, ex);
            return new TimeLogger();
        }
    }
}
