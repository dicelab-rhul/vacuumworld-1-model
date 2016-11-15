package uk.ac.rhul.cs.dice.vacuumworld.crypto;

import java.io.Serializable;
import java.security.Provider;
import java.security.Security;

import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public abstract class AbstractEncryptor<T1 extends Serializable, T2 extends Serializable> implements Encryptor<T1, T2> {

    @Override
    public String getProviderName() {
	try {
	    return getProviderName(0);
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	    
	    return null;
	}
    }
    
    @Override
    public String getProviderName(int number) {
	try {
	    return getProvider(number).getName();
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	    
	    return null;
	}
    }

    @Override
    public Provider getProvider() {
	try {
	    return getProvider(0);
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	    
	    return null;
	}
    }
    
    @Override
    public Provider getProvider(int number) {
	try {
	    return Security.getProviders()[number];
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	    
	    return null;
	}
    }
}