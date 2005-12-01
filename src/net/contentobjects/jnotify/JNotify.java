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

package net.contentobjects.jnotify;

import java.io.IOException;

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
			try
			{
				_instance = (IJNotify) Class.forName("net.contentobjects.jnotify.win32.IJNotifyAdapterWin32").newInstance();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			throw new RuntimeException("Unsupported OS : " + osName);
		}
	}
	
	public static IJNotify get()
	{
		return _instance;
	}
	
	public int addWatch(String path, int mask, boolean watchSubtree, JNotifyListener listener) throws IOException
	{
		return _instance.addWatch(path, mask, watchSubtree, listener);
	}

	public boolean removeWatch(int watchId)
	{
		return _instance.removeWatch(watchId);
	}
}