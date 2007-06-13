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
 
package net.contentobjects.jnotify.linux.test;

import java.io.IOException;

import net.contentobjects.jnotify.linux.JNotify_linux;
import net.contentobjects.jnotify.linux.INotifyListener;


public class TestJNotifyLinux
{
	public static void main(String[] args) throws IOException
	{
		System.err.println("--- staring... ");
		JNotify_linux.setNotifyListener(new INotifyListener()
		{
			public void notify(String name, int wd1, int mask, int cookie)
			{
				System.err.println("name " + name + ", wd " + wd1 + ", mask "+ mask + " , cookie " + cookie);
			}
		});
		
		
		int wd = JNotify_linux.addWatch("/home/omry/tmp", JNotify_linux.IN_ALL_EVENT);
		System.err.println("wd : " + wd);
		
		try
		{
			Thread.sleep(50000);
		}
		catch (InterruptedException e1)
		{
			// nop
		}
	}
}
