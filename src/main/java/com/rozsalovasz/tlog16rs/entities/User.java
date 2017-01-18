package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.exceptions.NotNewMonthException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;

/**
 * A class to store the working months
 *
 * @author rlovasz
 */
@Entity
@Getter
public class User implements Principal {

    @Id
    @GeneratedValue
    int id;

    private final String name;
    private final String password;
    private final String salt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<WorkMonth> months = new ArrayList();

    /**
     *
     * @param name The name of user
     * @param password
     * @param salt
     */
    public User(String name, String password, String salt) {
        this.name = name;
        this.password = password;
        this.salt = salt;
    }

    /**
     * This method adds a new month to the TimeLogger, if it is in the same
     * year, as the earlier ones
     *
     * @param workMonth the month to add
     * @throws com.rozsalovasz.tlog16rs.exceptions.NotNewMonthException
     */
    public void addMonth(WorkMonth workMonth) throws NotNewMonthException {
        if (isNewMonth(workMonth)) {
            months.add(workMonth);
        } else {
            throw new NotNewMonthException("This month is already exists.");
        }

    }

    /**
     * This method decides if the work month is in the list of the months
     * already
     *
     * @param workMonth, the parameter about to decide
     * @return true, if it is new, false, if it is already exists
     */
    private boolean isNewMonth(WorkMonth workMonth) {
        boolean isNewMonth = true;
        for (WorkMonth wm : months) {
            if (wm.getMonthDate().equals(workMonth.getMonthDate())) {
                isNewMonth = false;
                break;
            }
        }
        return isNewMonth;
    }
}
