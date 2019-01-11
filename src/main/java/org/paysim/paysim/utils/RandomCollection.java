package org.paysim.paysim.utils;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import ec.util.MersenneTwisterFast;

public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private MersenneTwisterFast random;
    private double total = 0;

    public RandomCollection() {
        this.random = null;
    }

    public RandomCollection(MersenneTwisterFast random) {
        this.random = random;
    }

    public void add(double weight, E result) {
        if (weight > 0) {
            total += weight;
            map.put(total, result);
        }
    }

    public E next() {
        if (this.random == null) {
            throw new NullPointerException("The RNG must be initialized to pick a random element.");
        }
        if (this.map.isEmpty()){
            throw new IllegalStateException("The collection is empty");
        }

        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }

    public Collection<E> getCollection() {
        return map.values();
    }

    public void setRandom(MersenneTwisterFast random) {
        this.random = random;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}