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

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public double getNewlimit() {
        return newlimit;
    }

    public void setNewlimit(double newlimit) {
        this.newlimit = newlimit;
    }

    public List<String> getDeniedReasons() {
        return deniedReasons;
    }

    public void setDeniedReasons(List<String> deniedReasons) {
        this.deniedReasons = deniedReasons;
    }
}
