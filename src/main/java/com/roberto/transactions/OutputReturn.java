package com.roberto.transactions;

import java.util.List;

public class OutputReturn {
    private boolean approved;
    private double newlimit;
    private List<String> deniedReasons;

    public OutputReturn(boolean approved, double newlimit, List<String> deniedReasons) {
        this.approved = approved;
        this.newlimit = newlimit;
        this.deniedReasons = deniedReasons;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setNewlimit(double newlimit) {
        this.newlimit = newlimit;
    }
}
