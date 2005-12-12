/*******************************************************************************
 * JNotify - Allow java applications to register to File system events.
 * 
 * Copyright (C) 2005 - Content Objects
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 ******************************************************************************
 *
 * Content Objects, Inc., hereby disclaims all copyright interest in the
 * library `JNotify' (a Java library for file system events). 
 * 
 * Yahali Sherman, 21 November 2005
 *    Content Objects, VP R&D.
 *    
 ******************************************************************************
 * Author : Omry Yadan
 ******************************************************************************/
 

package net.contentobjects.jnotify.linux;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;


/** TODO : added by omry at Dec 6, 2005 : Unit test recursive listening.*/
/** TODO : added by omry at Dec 11, 2005 : Handle move events*/

public class JNotifyAdapterLinux implements IJNotify
{
	private final static boolean DEBUG_LINUX_INOTIFY = true;
	
	private Hashtable<Integer,Integer> _linuxWd2Wd;
	private Hashtable<Integer, WatchData> _id2Data;
	private static int _watchIDCounter = 0;

	public JNotifyAdapterLinux()
	{
		JNotify_linux.setNotifyListener(new INotifyListener()
		{
			public void notify(String name, int wd, int mask, int cookie)
			{
				notifyChangeEvent(name, wd, mask, cookie);
			}
		});

		_id2Data = new Hashtable();
		_linuxWd2Wd = new Hashtable();
	}

	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener)
		throws JNotifyException
	{
		// map mask to linux inotify mask.
		int linuxMask = 0;
		if ((mask & IJNotify.FILE_CREATED) != 0)
		{
			linuxMask |= JNotify_linux.IN_CREATE;
		}
		if ((mask & IJNotify.FILE_DELETED) != 0)
		{
			linuxMask |= JNotify_linux.IN_DELETE;
			linuxMask |= JNotify_linux.IN_DELETE_SELF;
		}
		if ((mask & IJNotify.FILE_MODIFIED) != 0)
		{
			linuxMask |= JNotify_linux.IN_ATTRIB;
			linuxMask |= JNotify_linux.IN_MODIFY;
		}
		if ((mask & IJNotify.FILE_RENAMED) != 0)
		{
			linuxMask |= JNotify_linux.IN_MOVED_FROM;
			linuxMask |= JNotify_linux.IN_MOVED_TO;
		}
		
		// if watching subdirs, listen on create anyway.
		// to know when new sub directories are created.
		// these events should not reach the client code.
		if (watchSubtree)
		{
			linuxMask |= JNotify_linux.IN_CREATE;	
		}

		WatchData watchData = createWatch(-1, true, path, mask, linuxMask, watchSubtree, listener);
		if (watchSubtree)
		{
			try
			{
				File file = new File(path);
				registerToSubTree(true,watchData, file);
			}
			catch (JNotifyException e)
			{
				// cleanup
				removeWatch(watchData._wd, true);
				// and throw.
				throw e;
			}
		}
		return watchData._wd;
	}
	
	private WatchData createWatch(int parentWd, boolean user,String path, int mask, int linuxMask, boolean watchSubtree, JNotifyListener listener) throws JNotifyException
	{
		int wd = _watchIDCounter++;
		int linuxWd = JNotify_linux.addWatch(path, linuxMask);
		WatchData watchData = new WatchData(parentWd, user, path, wd, linuxWd, mask, linuxMask, watchSubtree, listener);
		_linuxWd2Wd.put(linuxWd, wd);
		_id2Data.put(wd, watchData);
		return watchData;
	}
	

	private void registerToSubTree(boolean isRoot, WatchData parentWatch, File root) throws JNotifyException
	{
		if (root.isDirectory())
		{
			String rootDir = root.getAbsolutePath();
			// root was already registered by the calling method.
			if (!isRoot)
			{
				try
				{
					String path = root.getAbsolutePath();
					WatchData subWatch = createWatch(parentWatch._wd, false, path, parentWatch._mask, parentWatch._linuxMask, parentWatch._watchSubtree, null);
					parentWatch.addSubwatch(subWatch._wd);
				}
				catch (JNotifyException e)
				{
					System.out.println("registerToSubTree : warning, failed to register " + rootDir + " :" + e.getMessage());
					if (e.getErrorCode() == JNotifyException.ERROR_WATCH_LIMIT_REACHED)
					{
						throw e;
					}
					// else, on any other error, try subtree anyway..
				}
			}
			
			String files[] = root.list();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					String file = files[i];
					registerToSubTree(false, parentWatch, new File(root, file));
				}
			}
		}
	}

	public boolean removeWatch(int wd)
	{
		return removeWatch(wd, true);
	}
	
	private boolean removeWatch(int wd, boolean removeLinuxWatch)
	{
		synchronized (_id2Data)
		{
			if (_id2Data.containsKey(wd))
			{
				WatchData watchData = _id2Data.remove(wd);
				unwatch(watchData, removeLinuxWatch);
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	
	
	private void unwatch(WatchData data, boolean removeLinuxWatch)
	{
		if (removeLinuxWatch)
		{
			int res = JNotify_linux.removeWatch(data._linuxWd);
			if (res == -1)
			{
				System.out.println("JNotifyAdapterLinux: warning, error removing linux watch " + data._linuxWd);
			}
		}
		
		_linuxWd2Wd.remove(data._linuxWd);
		_id2Data.remove(data._wd);
		if (data._user)
		{
			for (int i = 0; i < data._subWd.size(); i++)
			{
				int wd = data._subWd.get(i);
				removeWatch(wd, true);
			}
		}
	}

	protected void notifyChangeEvent(String name, int linuxWd, int linuxMask, int cookie)
	{
		synchronized (_id2Data)
		{
			Integer iwd = _linuxWd2Wd.get(linuxWd);
			if (iwd == null)
			{
				// This happens if an exception is thrown because used too many watches. 
				System.out.println("JNotifyAdapterLinux: warning, recieved event for an unregisted LinuxWD "+ linuxWd +" ignoring...");
				debugLinux(null, name, linuxMask);
				return;
			}
			
			WatchData watchData = _id2Data.get(iwd);
			if (DEBUG_LINUX_INOTIFY)
			{
				debugLinux(watchData, name, linuxMask);
			}
			
			if (watchData != null)
			{
				int externalWatchID = watchData._user ? watchData._wd : watchData._parentWd;
				if ((linuxMask & JNotify_linux.IN_CREATE) != 0)
				{
					// make sure user really requested to be notified on this event.
					// (in case of recursive listening, this flag is turned on anyway).
					if ((watchData._mask & FILE_CREATED) != 0)
					{
						watchData._notifyListener.fileCreated(externalWatchID, watchData._path, name);
					}
					
					if (watchData._watchSubtree)
					{
						String newDirPath = new File(watchData._path, name).getAbsolutePath();
						try
						{
							createWatch(externalWatchID, false, newDirPath, watchData._mask, watchData._linuxMask, watchData._watchSubtree, watchData._notifyListener);
						}
						catch (JNotifyException e)
						{
							System.out.println("registerToSubTree : warning, failed to register " + newDirPath + " :" + e.getMessage());
						}
					}
				}
				else
				if ((linuxMask & JNotify_linux.IN_IGNORED) != 0)
				{
					removeWatch(iwd, false);
				}
//				else
//				if ((linuxMask & JNotify_linux.IN_DELETE_SELF) != 0)
//				{
//				}
				else
				if ((linuxMask & JNotify_linux.IN_DELETE)  != 0)
				{
					watchData._notifyListener.fileDeleted(externalWatchID, watchData._path, name);
				}
				else
				if ((linuxMask & JNotify_linux.IN_ATTRIB) != 0 || (linuxMask & JNotify_linux.IN_MODIFY) != 0)
				{
					watchData._notifyListener.fileModified(externalWatchID, watchData._path, name);
				}
				else
				if ((linuxMask & JNotify_linux.IN_MOVED_FROM) != 0)
				{
					watchData._cookieToOldName.put(cookie, name);
				}
				else
				if ((linuxMask & JNotify_linux.IN_MOVED_TO) != 0)
				{
					String oldName = watchData._cookieToOldName.get(cookie);
					watchData._cookieToOldName.remove(cookie);
					watchData._notifyListener.fileRenamed(externalWatchID, watchData._path, oldName, name);
				}
			}
			else
			{
				System.out.println("JNotifyAdapterLinux: warning, recieved event for an unregisted WD " +  iwd + ". ignoring...");
			}
		}
	}

	private void debugLinux(WatchData watchData, String name, int linuxMask)
	{
		boolean IN_ACCESS = (linuxMask & JNotify_linux.IN_ACCESS) != 0;
		boolean IN_MODIFY = (linuxMask & JNotify_linux.IN_MODIFY) != 0;
		boolean IN_ATTRIB = (linuxMask & JNotify_linux.IN_ATTRIB) != 0;
		boolean IN_CLOSE_WRITE = (linuxMask & JNotify_linux.IN_CLOSE_WRITE) != 0;
		boolean IN_CLOSE_NOWRITE = (linuxMask & JNotify_linux.IN_CLOSE_NOWRITE) != 0;
		boolean IN_OPEN = (linuxMask & JNotify_linux.IN_OPEN) != 0;
		boolean IN_MOVED_FROM = (linuxMask & JNotify_linux.IN_MOVED_FROM) != 0;
		boolean IN_MOVED_TO = (linuxMask & JNotify_linux.IN_MOVED_TO) != 0;
		boolean IN_CREATE = (linuxMask & JNotify_linux.IN_CREATE) != 0;
		boolean IN_DELETE = (linuxMask & JNotify_linux.IN_DELETE) != 0;
		boolean IN_DELETE_SELF = (linuxMask & JNotify_linux.IN_DELETE_SELF) != 0;
		boolean IN_MOVE_SELF = (linuxMask & JNotify_linux.IN_MOVE_SELF) != 0;
		boolean IN_UNMOUNT = (linuxMask & JNotify_linux.IN_UNMOUNT) != 0;
		boolean IN_Q_OVERFLOW = (linuxMask & JNotify_linux.IN_Q_OVERFLOW) != 0;
		boolean IN_IGNORED = (linuxMask & JNotify_linux.IN_IGNORED) != 0;
		String s ="";
		if (IN_ACCESS) s += "IN_ACCESS, ";
		if (IN_MODIFY) s += "IN_MODIFY, ";
		if (IN_ATTRIB) s += "IN_ATTRIB, ";
		if (IN_CLOSE_WRITE) s += "IN_CLOSE_WRITE, ";
		if (IN_CLOSE_NOWRITE) s += "IN_CLOSE_NOWRITE, ";
		if (IN_OPEN) s += "IN_OPEN, ";
		if (IN_MOVED_FROM) s += "IN_MOVED_FROM, ";
		if (IN_MOVED_TO) s += "IN_MOVED_TO, ";
		if (IN_CREATE) s += "IN_CREATE, ";
		if (IN_DELETE) s += "IN_DELETE, ";
		if (IN_DELETE_SELF) s += "IN_DELETE_SELF, ";
		if (IN_MOVE_SELF) s += "IN_MOVE_SELF, ";
		if (IN_UNMOUNT) s += "IN_UNMOUNT, ";
		if (IN_Q_OVERFLOW) s += "IN_Q_OVERFLOW, ";
		if (IN_IGNORED) s += "IN_IGNORED, ";
		System.out.println(s + " : " +(watchData != null ? watchData : name));
	}

	private static class WatchData
	{
		int _parentWd;
		boolean _user;
		int _wd;
		private int _linuxWd;
		private ArrayList<Integer> _subWd;
		int _mask; 
		int _linuxMask;
		boolean _watchSubtree;
		JNotifyListener _notifyListener;
		public String renameOldName;
		Hashtable<Integer, String> _cookieToOldName = new Hashtable();
		String _path;

		WatchData(int parnetWd,boolean user, String path, int wd, int linuxWd, int mask, int linuxMask, boolean watchSubtree, JNotifyListener listener)
		{
			_parentWd = parnetWd;
			_user = user;
			_subWd = new ArrayList();
			_path = path;
			_wd = wd;
			_linuxMask = linuxMask;
			_linuxWd = linuxWd;
			_mask = mask;
			_watchSubtree = watchSubtree;
			_notifyListener = listener;
		}
		
		void remveSubwatch(int subWatch)
		{
			_subWd.remove(subWatch);
		}

		void addSubwatch(int subWatch)
		{
			_subWd.add(subWatch);
		}
		
		@Override
		public String toString()
		{
			return "WatchData " + _path + ", wd=" +  _wd + ", linuxWd=" + _linuxWd  + (_watchSubtree ? ", recursive" :"") + (_user ? ", user" : ", auto");
		}
	}
}
