package net.contentobjects.jnotify.macosx;

import net.contentobjects.jnotify.JNotifyException;

public class JNotify_macosx
{
	static
	{
		System.loadLibrary("jnotify");
		int res = nativeInit();
		if (res != 0)
		{
			throw new RuntimeException("Error initializing native library. (#" + res + ")");
		}
	}

	private static native int nativeInit();
	private static native int nativeAddWatch(String path, boolean watchSubtree) throws JNotifyException;
	private static native String getErrorDesc(long errorCode);
	private static native void nativeRemoveWatch(int wd);

	private static FSEventListener _eventListener;

	public static int addWatch(String path, boolean watchSubtree) throws JNotifyException
	{
		return nativeAddWatch(path, watchSubtree);
	}

	public static void removeWatch(int wd)
	{
		nativeRemoveWatch(wd);
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
