package net.contentobjects.jnotify;

public class JNotify implements IJNotify
{
	private static IJNotify _instance;
	
	static 
	{
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.equals("linux"))
		{
		}
		else
		if (osName.startsWith("windows"))
		{
		}
		else
		{
			throw new RuntimeException("Unsupported OS");
		}
	}
	
	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener)
	{
		return _instance.addWatch(path, mask, watchSubtree, listener);
	}

	public boolean removeWatch(int watchId)
	{
		return _instance.removeWatch(watchId);
	}
}
