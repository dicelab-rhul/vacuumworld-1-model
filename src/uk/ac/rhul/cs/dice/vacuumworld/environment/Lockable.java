package uk.ac.rhul.cs.dice.vacuumworld.environment;

public interface Lockable {
    public void getSharedReadLock() throws AlreadyLockedException;
    public void getExclusiveWriteLock() throws AlreadyLockedException;
    public void releaseSharedReadLock();
    public void releaseExclusiveWriteLock();
}