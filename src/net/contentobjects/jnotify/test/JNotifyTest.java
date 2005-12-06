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
 
package net.contentobjects.jnotify.test;

import java.io.IOException;

import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;


public class JNotifyTest
{

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		int watchID = JNotify.get().addWatch("/home/omry/tmp", IJNotify.FILE_ANY, true, new JNotifyListener()
		{
			public void fileRenamed(int wd, String rootPath, String oldName, String newName)
			{
				System.out.println("JNotifyTest.fileRenamed() : wd #" +wd + " root = " + rootPath + ", "  + oldName + " -> " + newName);
			}
		
			public void fileModified(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileModified() : wd #" +wd + " root = " + rootPath + ", "  + name);
			}
		
			public void fileDeleted(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileDeleted() : wd #" +wd + " root = " + rootPath + ", "  + name);
			}
		
			public void fileCreated(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileCreated() : wd #" +wd + " root = " + rootPath + ", "  + name);
			}
		});
		
		try
		{
			Thread.sleep(1000000);
		}
		catch (InterruptedException e1)
		{
		}
		
		// to remove watch:
		boolean res = JNotify.get().removeWatch(watchID);
		if (!res)
		{
			// failed to remove
		}
		
	}

}
