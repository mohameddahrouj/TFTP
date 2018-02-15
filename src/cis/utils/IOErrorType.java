package cis.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 
 * @author lavatahir
 *
 */
public enum IOErrorType {

	FileNotFound(1,"File not Found"),
	AccessViolation(2," Access violation"),
	DiskFull(3, "Disk full"),
	FileExists(6, "File exists");
	
	private int errorCode;
	private String errorMessage;
	
	private IOErrorType(int errorCode, String errorMessage)
	{
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
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
