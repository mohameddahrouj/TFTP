package cis.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class that enumerates the different IO error types.
 * Last edited February 17th, 2018
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public enum IOErrorType {

	FileNotFound(1,"File not Found"),
	AccessViolation(2," Access violation"),
	DiskFull(3, "Disk full"),
	IllegalOperation(4,"Illegal Operation."),
	UnkownTransferID(5,"Unkown Operation"),
	FileExists(6, "File exists");
	
	private int errorCode;
	private String errorMessage;
	
	private IOErrorType(int errorCode, String errorMessage)
	{
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public int getErrorCode()
	{
		return this.errorCode;
	}
	
	public String getErrorMessage()
	{
		return this.errorMessage;
	}
	
	public byte[] createErrorPacketData()
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			byteArrayOutputStream.write(0);
			byteArrayOutputStream.write(5);
			byteArrayOutputStream.write(0);
			byteArrayOutputStream.write(this.errorCode);
			byteArrayOutputStream.write(this.errorMessage.getBytes());
			byteArrayOutputStream.write(0);
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
			System.exit(1);
		}

		return byteArrayOutputStream.toByteArray();
	}
}
