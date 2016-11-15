package uk.ac.rhul.cs.dice.vacuumworld.crypto;

import java.security.Key;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.RuntimeCryptoException;

import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class VacuumWorldEncryptor extends AbstractEncryptor<String, String> {
    private String providerName;
    
    public VacuumWorldEncryptor() {
	VWUtils.checkProvider();
	
	this.providerName = Security.getProvider("BC").toString();
    }
    
    @Override
    public VacuumWorldEncryptedData encrypt(String ptx, Key key, String algorithm, byte[] iv, String aad, int tagLen) {
	try {
	    byte[] binaryPtx = ptx.getBytes();
	    Cipher cipher = Cipher.getInstance(algorithm, this.providerName);
	    AlgorithmParameterSpec spec = generateAlgorithmParameterSpec(algorithm, iv, tagLen);
	    cipher.init(Cipher.ENCRYPT_MODE, key, spec);
	    updateAADIfNecessary(cipher, aad);
	    
	    byte[] ctx = cipher.doFinal(binaryPtx);
	    
	    return new VacuumWorldEncryptedData(ctx, aad, tagLen);
	}
	catch(Exception e) {
	    RuntimeCryptoException exception = new RuntimeCryptoException(e.getMessage());
	    exception.initCause(e);
	    
	    throw exception;
	}
    }

    private void updateAADIfNecessary(Cipher cipher, String aad) {
	try {
	    if(aad != null) {
		cipher.updateAAD(aad.getBytes());
	    }
	}
	catch(IllegalStateException | UnsupportedOperationException e) {
	    VWUtils.fakeLog(e);
	}
    }

    private AlgorithmParameterSpec generateAlgorithmParameterSpec(String algorithm, byte[] iv, int tagLen) {
	if(!algorithm.contains("GCM")) {
	    return new IvParameterSpec(iv);
	}
	else if(!Arrays.asList(128, 120, 112, 104, 96).contains(tagLen)) {
	    throw new IllegalArgumentException("Bad tag length: " + tagLen);
	}
	else { 
	    return new GCMParameterSpec(tagLen, iv);
	}
    }

    @Override
    public VacuumWorldEncryptedData encrypt(String ptx, byte[] encodedKey, String keySpec, String algorithm, byte[] iv, String aad, int tagLen) {
	Key key = new SecretKeySpec(encodedKey, algorithm);
	
	return encrypt(ptx, key, algorithm, iv, aad, tagLen);
    }

    @Override
    public String decrypt(EncryptedDataInterface<String> ctx, Key key, String algorithm, byte[] iv) {
	try {
	    byte[] binaryCtx = ctx.getEncryptedData();
	    int tagLen = ctx.getTagLen();
	    String aad = ctx.getAdditionalAuthenticatedData();
	    
	    Cipher cipher = Cipher.getInstance(algorithm, this.providerName);
	    AlgorithmParameterSpec spec = generateAlgorithmParameterSpec(algorithm, iv, tagLen);
	    cipher.init(Cipher.DECRYPT_MODE, key, spec);
	    updateAADIfNecessary(cipher, aad);
	    
	    byte[] ptx = cipher.doFinal(binaryCtx);
	    
	    return new String(ptx);
	}
	catch(Exception e) {
	    RuntimeCryptoException exception = new RuntimeCryptoException(e.getMessage());
	    exception.initCause(e);
	    
	    throw exception;
	}
    }

    @Override
    public String decrypt(EncryptedDataInterface<String> ctx, byte[] encodedKey, String keySpec, String algorithm, byte[] iv) {
	Key key = new SecretKeySpec(encodedKey, algorithm);
	
	return decrypt(ctx, key, algorithm, iv);
    }
}