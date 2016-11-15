package uk.ac.rhul.cs.dice.vacuumworld.crypto;

import java.io.Serializable;

public interface EncryptedDataInterface<T extends Serializable> extends Serializable {
    public int getTagLen();
    public abstract byte[] getEncryptedData();
    public abstract T getAdditionalAuthenticatedData();
    public abstract boolean isEncryptedDataPresent();
    public abstract boolean isAADPresent();
}