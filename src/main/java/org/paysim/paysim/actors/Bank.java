package org.paysim.paysim.actors;

public class Bank extends SuperActor {
    private static final String BANK_IDENTIFIER = "B";

    public Bank(String name) {
        super(BANK_IDENTIFIER + name);
    }
}
