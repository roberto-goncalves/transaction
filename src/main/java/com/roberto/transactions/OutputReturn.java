package com.roberto.transactions;

import java.math.BigDecimal;
import java.util.List;

public class OutputReturn {
    private boolean approved;
    private BigDecimal newlimit;
    private List<String> deniedReasons;

    public OutputReturn(boolean approved, BigDecimal newlimit, List<String> deniedReasons) {
        this.approved = approved;
        this.newlimit = newlimit;
        this.deniedReasons = deniedReasons;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setNewlimit(BigDecimal newlimit) {
        this.newlimit = newlimit;
    }
}
