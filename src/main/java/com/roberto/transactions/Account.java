package com.roberto.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Account {

    @JsonProperty("cardIsActive")
    private boolean isCardActive;
    @JsonProperty("limit")
    private double limit;
    @JsonProperty("blacklist")
    private List<String> blacklist;
    @JsonProperty("isWhiteListed")
    private boolean isWhiteListed;

    public Account(boolean cardIsActive, double limit, List blacklist, boolean isWhiteListed) {
        this.isCardActive = cardIsActive;
        this.limit = limit;
        this.blacklist = blacklist;
        this.isWhiteListed = isWhiteListed;
    }

    public Account(Account account) {
        this.isCardActive = account.isCardActive;
        this.limit = account.limit;
        this.blacklist = account.blacklist;
        this.isWhiteListed = account.isWhiteListed;
    }

    public boolean isCardActive() {
        return isCardActive;
    }

    public double getLimit() {
        return limit;
    }

    public List getBlacklist() {
        return blacklist;
    }

    public boolean isWhiteListed() {
        return isWhiteListed;
    }

}
