package uk.ac.rhul.cs.dice.vacuumworld.crypto;

import java.io.Serializable;
import java.security.Key;
import java.security.Provider;

public interface Encryptor<T1 extends Serializable, T2 extends Serializable> {
    public abstract String getProviderName();
    public abstract String getProviderName(int number);
    public abstract Provider getProvider();
    public abstract Provider getProvider(int number);
    public abstract EncryptedDataInterface<T2> encrypt(T1 ptx, Key key, String algorithm, byte[] iv, T2 aad, int tagLen);
    public abstract EncryptedDataInterface<T2> encrypt(T1 ptx, byte[] encodedKey, String keySpec, String algorithm, byte[] iv, T2 aad, int tagLen);
    public abstract T1 decrypt(EncryptedDataInterface<T2> ctx, Key key, String algorithm, byte[] iv);
    public abstract T1 decrypt(EncryptedDataInterface<T2> ctx, byte[] encodedKey, String keySpec, String algorithm, byte[] iv);
}