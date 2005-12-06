package net.contentobjects.jnotify;

import java.io.IOException;

public abstract class JNotifyException extends IOException
{
	public static int ERROR_UNSPECIFIED = 1;
	public static int ERROR_WATCH_LIMIT_REACHED = 2;
	public static int ERROR_PERMISSION_DENIED = 3;
	
	
	protected final int _systemErrorCode;
	
	public JNotifyException(String s, int systemErrorCode)
	{
		super(s);
		_systemErrorCode = systemErrorCode;
	}
	
	public int getSystemError()
	{
		return _systemErrorCode;
	}
	
	public abstract int getErrorCode();
}
