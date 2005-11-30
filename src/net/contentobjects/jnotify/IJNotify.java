package net.contentobjects.jnotify;

public interface IJNotify
{
	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener);
	public boolean removeWatch(int watchId);
}
