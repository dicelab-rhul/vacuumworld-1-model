package uk.ac.rhul.cs.dice.vacuumworld.crypto;

import java.io.Serializable;

public class AbstractEncryptedData<T extends Serializable> implements EncryptedDataInterface<T> {
    private static final long serialVersionUID = -7527395331868217658L;
    private int tagLen;
    private byte[] encryptedData;
    private T additionalAuthenticatedData;
    
    public AbstractEncryptedData(byte[] encryptedData, T additionalAuthenticatedData, int tagLen) {
	this.encryptedData = encryptedData;
	this.additionalAuthenticatedData = additionalAuthenticatedData;
	this.tagLen = tagLen;
    }
    
    public AbstractEncryptedData(byte[] encryptedData) {
	this.encryptedData = encryptedData;
	this.additionalAuthenticatedData = null;
	this.tagLen = 0;
    }
    
    @Override
    public int getTagLen() {
        return this.tagLen;
    }
    
    @Override
    public byte[] getEncryptedData() {
	return this.encryptedData;
    }

    @Override
    public T getAdditionalAuthenticatedData() {
	return this.additionalAuthenticatedData;
    }

    @Override
    public boolean isEncryptedDataPresent() {
	return this.encryptedData != null;
    }
    
    @Override
    public boolean isAADPresent() {
	return this.additionalAuthenticatedData != null;
    }
}