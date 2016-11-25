package uk.ac.rhul.cs.dice.vacuumworld.utils;

@FunctionalInterface
public interface Duplicable<T> {
    public abstract T duplicate();
}