package net.contentobjects.jnotify.macosx;

import java.util.Hashtable;

import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyAdapterMacOSX implements IJNotify
{
	private Hashtable _id2Data;

	public JNotifyAdapterMacOSX()
	{
		JNotify_macosx.setNotifyListener(new FSEventListener()
		{
			public void notifyChange(int wd, String rootPath, String filePath, boolean recurse)
			{
				notifyChangeEvent(wd, rootPath, filePath, recurse);
			}
		});
		_id2Data = new Hashtable();
	}

	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener)
		throws JNotifyException
	{
		int wd = JNotify_macosx.addWatch(path);
		_id2Data.put(Integer.valueOf(wd), new WatchData(wd, mask, listener));
		return wd;
	}

	public boolean removeWatch(int wd) throws JNotifyException
	{
		synchronized (_id2Data)
		{
			boolean removed = _id2Data.remove(Integer.valueOf(wd)) != null;
			if (removed)
			{
				JNotify_macosx.removeWatch(wd);
			}
			return removed;
		}
	}

	private static class WatchData
	{
		int _wd;
		int _mask;
		JNotifyListener _notifyListener;
		public String renameOldName;

		WatchData(int wd, int mask, JNotifyListener listener)
		{
			_wd = wd;
			_mask = mask;
			_notifyListener = listener;
		}
		
		public String toString()
		{
			return "wd=" + _wd;
		}
	}

	void notifyChangeEvent(int wd, String rootPath, String filePath, boolean recurse)
	{
		synchronized (_id2Data)
		{
			WatchData watchData = (WatchData) _id2Data.get(Integer.valueOf(wd));
			if (watchData != null)
			{
				int mask = watchData._mask;
				watchData._notifyListener.fileModified(wd, rootPath, filePath);
				watchData._notifyListener.fileDeleted(wd, rootPath, filePath);
				watchData.renameOldName = filePath;
				watchData._notifyListener.fileRenamed(wd, rootPath, watchData.renameOldName, filePath);
				watchData.renameOldName = null;
			}
		}
	}
}
