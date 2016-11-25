package uk.ac.rhul.cs.dice.vacuumworld.threading;

@FunctionalInterface
public interface ThreadState {
    public void run(AbstractActorRunnable runnable);
}