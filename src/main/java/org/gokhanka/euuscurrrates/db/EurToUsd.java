package org.gokhanka.euuscurrrates.db;
/**
 * the database object
 * @author gokhanka
 *
 */
public class EurToUsd {
    private int doneDate = 0;
    private String rate = null;
    
    public int getDoneDate() {
        return doneDate;
    }
    public void setDoneDate(int doneDate) {
        this.doneDate = doneDate;
    }
    public String getRate() {
        return rate;
    }
    public void setRate(String rate) {
        this.rate = rate;
    }
}
