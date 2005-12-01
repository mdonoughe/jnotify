package net.contentobjects.jnotify.linux;

import java.io.IOException;

import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyAdapterLinux implements IJNotify
{

	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener) throws IOException
	{
		return 0;
	}

	public boolean removeWatch(int wd)
	{
		// TODO Auto-generated method stub
		return false;
	}
}
