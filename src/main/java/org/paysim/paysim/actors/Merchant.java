package org.paysim.paysim.actors;

public class Merchant extends SuperActor {
    private static final String MERCHANT_IDENTIFIER = "M";

    public Merchant(String name) {
        super(MERCHANT_IDENTIFIER + name);
    }
}
