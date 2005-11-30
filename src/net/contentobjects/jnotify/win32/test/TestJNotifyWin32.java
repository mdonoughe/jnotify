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
 

package net.contentobjects.jnotify.win32.test;

import java.io.IOException;

import net.contentobjects.jnotify.win32.JNotify_win32;


public class TestJNotifyWin32
{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException
	{
		int wd = JNotify_win32.addWatch("c:/test/", JNotify_win32.FILE_ACTION_ANY, true);
		sleep1(1000);
		int wd1 = JNotify_win32.addWatch("c:/Junk/", JNotify_win32.FILE_ACTION_ANY, true);
		sleep1(1000000);
	}

	private static void sleep1(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e1)
		{
			// nop
		}
	}
}
