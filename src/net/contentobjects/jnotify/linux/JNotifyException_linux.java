package net.contentobjects.jnotify.linux;

import net.contentobjects.jnotify.JNotifyException;

public class JNotifyException_linux extends JNotifyException
{
	private static final int LINUX_PERMISSION_DENIED = 13;
	private static final int LINUX_NO_SPACE_LEFT_ON_DEVICE = 28;

	public JNotifyException_linux(String s, int systemErrorCode)
	{
		super(s, systemErrorCode);
	}

	@Override
	public int getErrorCode()
	{
		switch (_systemErrorCode)
		{
		case LINUX_PERMISSION_DENIED:
			return ERROR_PERMISSION_DENIED;
		case LINUX_NO_SPACE_LEFT_ON_DEVICE:
			return ERROR_WATCH_LIMIT_REACHED;
		default:
			return ERROR_UNSPECIFIED;
		}
	}

}
