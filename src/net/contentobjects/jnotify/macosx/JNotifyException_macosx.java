package net.contentobjects.jnotify.macosx;

import net.contentobjects.jnotify.JNotifyException;

public class JNotifyException_macosx extends JNotifyException
{

	public JNotifyException_macosx(String s, int systemErrorCode)
	{
		super(s, systemErrorCode);
	}

	public int getErrorCode()
	{
		return _systemErrorCode;
	}

}
