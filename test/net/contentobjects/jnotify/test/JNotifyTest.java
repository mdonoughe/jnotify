package net.contentobjects.jnotify.test;

import java.io.IOException;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class JNotifyTest
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args)
	{
		try
		{
			// to add a watch : 
			String path = System.getProperty("user.home") + "/tmp/";
			//		path += "/dev/";
			int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
					| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
			boolean watchSubtree = true;
			System.err.println("Adding a watch on " + path);
			int watchID = JNotify.addWatch(path, mask, watchSubtree,
					new JNotifyListener()
					{
						public void fileRenamed(int wd, String rootPath,
								String oldName, String newName)
						{
							System.out
									.println("JNotifyTest.fileRenamed() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ oldName + " -> " + newName);
						}

						public void fileModified(int wd, String rootPath,
								String name)
						{
							System.out
									.println("JNotifyTest.fileModified() : wd #"
											+ wd
											+ " root = "
											+ rootPath
											+ ", "
											+ name);
						}

						public void fileDeleted(int wd, String rootPath,
								String name)
						{
							System.out
									.println("JNotifyTest.fileDeleted() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ name);
						}

						public void fileCreated(int wd, String rootPath,
								String name)
						{
							System.out
									.println("JNotifyTest.fileCreated() : wd #"
											+ wd + " root = " + rootPath + ", "
											+ name);
						}
					});

			System.err.println("done");

			try
			{
				Thread.sleep(2000000);
			} catch (InterruptedException e1)
			{
			}

			// to remove watch:
			boolean res = JNotify.removeWatch(watchID);
			if (!res)
			{
				// invalid watch ID specified.
			}
			
		} 
		catch (JNotifyException e)
		{
			switch (e.getErrorCode())
			{
			case JNotifyException.ERROR_NO_SUCH_FILE_OR_DIRECTORY:
				System.err.println("JNotifyException.ERROR_NO_SUCH_FILE_OR_DIRECTORY");
				break;
			case JNotifyException.ERROR_PERMISSION_DENIED:
				System.err.println("JNotifyException.ERROR_PERMISSION_DENIED");
				break;
			case JNotifyException.ERROR_WATCH_LIMIT_REACHED:
				System.err.println("JNotifyException.ERROR_WATCH_LIMIT_REACHED");
				break;
			case JNotifyException.ERROR_UNSPECIFIED:
				System.err.println("JNotifyException.ERROR_UNSPECIFIED");
				break;
			default:
				break;
			}
			e.printStackTrace();
			
		}
	}

}
