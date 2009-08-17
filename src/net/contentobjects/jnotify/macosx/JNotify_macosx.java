package net.contentobjects.jnotify.macosx;

import net.contentobjects.jnotify.JNotifyException;

public class JNotify_macosx
{
	private static Object initCondition = new Object();
	private static Object countLock = new Object();
	private static int watches = 0;

	static
	{
		System.loadLibrary("jnotify");
		Thread thread = new Thread("FSEvent thread")
		{
			public void run()
			{
				nativeInit();
				synchronized (initCondition)
				{
					initCondition.notifyAll();
					initCondition = null;
				}
				while (true)
				{
					synchronized (countLock)
					{
						while (watches == 0)
						{
							try
							{
								countLock.wait();
							}
							catch (InterruptedException e)
							{
							}
						}
					}
					nativeNotifyLoop();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	private static native void nativeInit();
	private static native int nativeAddWatch(String path) throws JNotifyException;
	private static native String getErrorDesc(long errorCode);
	private static native boolean nativeRemoveWatch(int wd);
	private static native void nativeNotifyLoop();

	private static FSEventListener _eventListener;

	public static int addWatch(String path) throws JNotifyException
	{
		Object myCondition = initCondition;
		if (myCondition != null)
		{
			synchronized (myCondition)
			{
				while (initCondition != null)
				{
					try
					{
						initCondition.wait();
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		}
		int wd = nativeAddWatch(path);
		synchronized (countLock)
		{
			watches++;
			countLock.notifyAll();
		}
		return wd;
	}

	public static boolean removeWatch(int wd)
	{
		boolean removed = nativeRemoveWatch(wd);
		if (removed)
		{
			synchronized (countLock)
			{
				watches--;
			}
		}
		return removed;
	}

	public static void callbackProcessEvent(int wd, String rootPath, String filePath, boolean recurse)
	{
		if (_eventListener != null)
		{
			_eventListener.notifyChange(wd, rootPath, filePath, recurse);
		}
	}

	public static void setNotifyListener(FSEventListener eventListener)
	{
		if (_eventListener == null)
		{
			_eventListener = eventListener;
		}
		else
		{
			throw new RuntimeException("Notify listener is already set. multiple notify listeners are not supported.");
		}
	}
}
