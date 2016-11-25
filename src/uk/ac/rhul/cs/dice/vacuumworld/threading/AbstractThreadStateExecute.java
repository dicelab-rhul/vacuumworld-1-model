package uk.ac.rhul.cs.dice.vacuumworld.threading;

public abstract class AbstractThreadStateExecute implements ThreadState {

    @Override
    public void run(AbstractActorRunnable runnable) {
	runnable.getActorMind().execute(null);
    }

    @Override
    public String toString() {
	return this.getClass().getSimpleName();
    }
}