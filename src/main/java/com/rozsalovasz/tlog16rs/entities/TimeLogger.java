package com.rozsalovasz.tlog16rs.entities;

import com.rozsalovasz.tlog16rs.core.NotNewMonthException;
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
import lombok.Setter;
/**
 * A class to store the working months
 *
 * @author rlovasz
 */
@Entity
@Getter
public class TimeLogger implements Principal{

	@Id
	@GeneratedValue
	@Setter
	int id;

	String name;
	String password;
	String salt;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkMonth> months = new ArrayList();

	/**
	 *
	 * @param name The name of user
	 * @param password
	 * @param salt
	 */
	public TimeLogger(String name, String password, String salt) {
		this.name = name;
		this.password = password;
		this.salt = salt;
	}

    /**
     * This method adds a new month to the TimeLogger, if it is in the same
     * year, as the earlier ones
     *
     * @param workMonth the month to add
     */
    public void addMonth(WorkMonth workMonth) {
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
        for (WorkMonth wm : months) {
			if ((workMonth.getMonthDate().equals(wm.getMonthDate())) && !months.isEmpty()) {
                return false;
            }
        }
        return true;
	}
}
