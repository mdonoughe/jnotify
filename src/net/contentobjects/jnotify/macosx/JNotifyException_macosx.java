package net.contentobjects.jnotify.macosx;

import net.contentobjects.jnotify.JNotifyException;

public class JNotifyException_macosx extends JNotifyException
{

	public JNotifyException_macosx(String s)
	{
		super(s, 1);
	}

	public int getErrorCode()
	{
		return 1;
	}

}
