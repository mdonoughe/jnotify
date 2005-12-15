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
		String path = "/home/omry/tmp";
		int mask = IJNotify.FILE_CREATED | IJNotify.FILE_DELETED | IJNotify.FILE_MODIFIED
				| IJNotify.FILE_RENAMED;
		boolean watchSubtree = true;
		int watchID = JNotify.get().addWatch(path, mask, watchSubtree, new JNotifyListener()
		{
			public void fileRenamed(int wd, String rootPath, String oldName, String newName)
			{
				System.out.println("JNotifyTest.fileRenamed() : wd #" + wd + " root = " + rootPath
						+ ", " + oldName + " -> " + newName);
			}

			public void fileModified(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileModified() : wd #" + wd + " root = " + rootPath
						+ ", " + name);
			}

			public void fileDeleted(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileDeleted() : wd #" + wd + " root = " + rootPath
						+ ", " + name);
			}

			public void fileCreated(int wd, String rootPath, String name)
			{
				System.out.println("JNotifyTest.fileCreated() : wd #" + wd + " root = " + rootPath
						+ ", " + name);
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
			// invalid watch ID specified.
		}
	}

}
