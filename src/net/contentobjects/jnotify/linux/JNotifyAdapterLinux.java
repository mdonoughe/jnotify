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


/** TODO : added by omry at Dec 6, 2005 : Debug recursive listening.*/

public class JNotifyAdapterLinux implements IJNotify
{
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


		
		int wd = _watchIDCounter++;
		int linuxWd = JNotify_linux.addWatch(path, linuxMask);
		WatchData watchData = new WatchData(path, wd, linuxWd, mask, linuxMask, watchSubtree, listener);
		_linuxWd2Wd.put(linuxWd, wd);
		_id2Data.put(wd, watchData);
		if (watchSubtree)
		{
			try
			{
				registerToSubTree(true,watchData, new File(path));
			}
			catch (JNotifyException e)
			{
				// cleanup
				removeWatch(wd);
				// and throw.
				throw e;
			}
		}
		return wd;
	}
	
	

	private void registerToSubTree(boolean isRoot, WatchData watchData, File root) throws JNotifyException
	{
		if (root.isDirectory())
		{
			String rootDir = root.getAbsolutePath();
			// root was already registered by the calling method.
			if (!isRoot)
			{
				try
				{
					watchSubdir(watchData, rootDir);
				}
				catch (JNotifyException e)
				{
					System.out.println("registerToSubTree : warning, failed to register " + rootDir + " :" + e.getMessage());
					if (e.getErrorCode() == JNotifyException.ERROR_WATCH_LIMIT_REACHED)
					{
						throw e;
					}
					// try subtree anyway..
				}
			}
			
			String files[] = root.list();
			if (files != null)
			{
				for (int i = 0; i < files.length; i++)
				{
					String file = files[i];
					registerToSubTree(false, watchData, new File(root, file));
				}
			}
		}
	}

	private void watchSubdir(WatchData watchData, String rootDir) throws JNotifyException
	{
		System.err.println("Registering sub tree " + rootDir + " under watch " + watchData._wd);
		int linuxWd = JNotify_linux.addWatch(rootDir, watchData._linuxMask);
		_linuxWd2Wd.put(linuxWd, watchData._wd);
		watchData._linuxWd.add(linuxWd);
	}

	public boolean removeWatch(int wd)
	{
		synchronized (_id2Data)
		{
			if (_id2Data.containsKey(wd))
			{
				WatchData data = _id2Data.remove(wd);
				for (int i = 0; i < data._linuxWd.size(); i++)
				{
					int linuxWd = data._linuxWd.get(i);
					_linuxWd2Wd.remove(linuxWd);
					JNotify_linux.removeWatch(linuxWd);
				}
				return true;
			}
			else
			{
				return false;
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
				System.err.println("JNotifyAdapterLinux: warning, recieved event for an unregisted LinuxWD "+ linuxWd +" ignoring...");
				return;
			}
			
			WatchData watchData = _id2Data.get(iwd);
			if (watchData != null)
			{
				if ((linuxMask & JNotify_linux.IN_CREATE) != 0)
				{
					// make sure user really requested to be notified on this event.
					// (in case of recursive listening, this flag is turned on anyway).
					if ((watchData._mask & FILE_CREATED) != 0)
					{
						watchData._notifyListener.fileCreated(watchData._wd, watchData._path, name);
					}
					
					if (watchData._watchSubtree)
					{
						String newDirPath = new File(watchData._path, name).getAbsolutePath();
						try
						{
							watchSubdir(watchData, newDirPath);
						}
						catch (JNotifyException e)
						{
							System.out.println("registerToSubTree : warning, failed to register " + newDirPath + " :" + e.getMessage());
						}
					}
				}
				else
				if ((linuxMask & JNotify_linux.IN_DELETE)  != 0 || (linuxMask & JNotify_linux.IN_DELETE_SELF) != 0)
				{
					watchData._notifyListener.fileDeleted(watchData._wd, watchData._path, name);
					
					// only remove watch if the actaul listened file was removed. 
					// (and not a file on a sub tree). 
					if ((linuxMask & JNotify_linux.IN_DELETE_SELF) != 0)
					{
						removeWatch(iwd);
					}
					
					// if this is a subwatch on a subtree, stop watching it.
					if (watchData._watchSubtree)
					{
						_linuxWd2Wd.remove(linuxWd);
						JNotify_linux.removeWatch(linuxWd);
					}
				}
				else
				if ((linuxMask & JNotify_linux.IN_ATTRIB) != 0 || (linuxMask & JNotify_linux.IN_MODIFY) != 0)
				{
					watchData._notifyListener.fileModified(watchData._wd, watchData._path, name);
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
					watchData._notifyListener.fileRenamed(watchData._wd, watchData._path, oldName, name);
				}
			}
			else
			{
				System.err.println("JNotifyAdapterLinux: warning, recieved event for an unregisted WD " +  iwd + ". ignoring...");
			}
		}
	}

	private static class WatchData
	{
		int _wd;
		// if watching subtree, we possibly watching more than one directory.
		private ArrayList<Integer> _linuxWd;
		int _mask;
		int _linuxMask;
		boolean _watchSubtree;
		JNotifyListener _notifyListener;
		public String renameOldName;
		Hashtable<Integer, String> _cookieToOldName = new Hashtable();
		String _path;

		WatchData(String path, int wd, int linuxWd, int mask, int linuxMask, boolean watchSubtree, JNotifyListener listener)
		{
			_path = path;
			_wd = wd;
			_linuxMask = linuxMask;
			_linuxWd = new ArrayList();
			_linuxWd.add(linuxWd);
			_mask = mask;
			_watchSubtree = watchSubtree;
			_notifyListener = listener;
		}
	}
}
