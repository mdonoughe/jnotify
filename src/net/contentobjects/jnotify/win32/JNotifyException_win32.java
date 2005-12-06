package net.contentobjects.jnotify.win32;

import net.contentobjects.jnotify.JNotifyException;

public class JNotifyException_win32 extends JNotifyException
{

	public JNotifyException_win32(String s, int systemErrorCode)
	{
		super(s, systemErrorCode);
	}

	@Override
	public int getErrorCode()
	{
		return ERROR_UNSPECIFIED;
	}

}
