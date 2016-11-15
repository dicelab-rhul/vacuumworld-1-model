package uk.ac.rhul.cs.dice.vacuumworld.crypto;

public class VacuumWorldEncryptedData extends AbstractEncryptedData<String> {
    private static final long serialVersionUID = -8063518057054243026L;

    public VacuumWorldEncryptedData(byte[] encryptedData, String additionalAuthenticatedData, int tagLen) {
	super(encryptedData, additionalAuthenticatedData, tagLen);
    }
}