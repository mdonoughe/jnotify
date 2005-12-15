/**
 * 
 */
package net.contentobjects.jnotify.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class Command
{
	static enum Action
	{
		DELETE, CREATE_FILE, CREATE_DIR, RENAME, MODIFY, SLEEP
	}
	
	private Command.Action _action;
	private String _path;
	private String _path2;
	private int _ms;

	public static Command delete(String path)
	{
		return new Command(Action.DELETE, path, null);
	}

	public static Command modify(String path)
	{
		return new Command(Action.MODIFY, path, null);
	}
	
	public static Command createFile(String path)
	{
		return new Command(Action.CREATE_FILE, path, null);
	}

	public static Command createDir(String path)
	{
		return new Command(Action.CREATE_DIR, path, null);
	}
	
	public static Command rename(String from, String to)
	{
		return new Command(Action.RENAME, from, to);
	}
	
	public static Command createSleep(int ms)
	{
		return new Command(ms);
	}
	
	
	private Command(int ms)
	{
		_action = Action.SLEEP;
		_ms = ms;
	}
	
	private Command(Command.Action action, String path, String path2)
	{
		_action = action;
		_path = path;
		_path2 = path2;
	}
	
	public boolean perform(File root) throws IOException
	{
		if (_action == Action.SLEEP)
		{
			try
			{
				Thread.sleep(_ms);
			}
			catch (InterruptedException e1)
			{
			}
			return true;
		}
		else
		{
			File file = new File(root, _path);
			if (_action == Action.CREATE_FILE)
			{
				System.out.println("Creating " + file);
				return file.createNewFile();
			}
			else
				if (_action == Action.CREATE_DIR)
				{
					return file.mkdir();
				}
				else
					if (_action == Action.DELETE)
					{
						System.out.println("Deleting " + file);
						return file.delete();
					}
					else
						if (_action == Action.MODIFY)
						{
							System.out.println("Modifying " + file);
							// just opening seems to raise a modify event.
							FileOutputStream out = new FileOutputStream(file);
							out.close();
							return true;
							
						}
						else
							if (_action == Action.RENAME)
							{
								System.out.println("Renaming " + file + " -> " + _path2);
								return file.renameTo(new File(root, _path2));
							}
		}

		// Unexpected action 
		throw new RuntimeException("Unexpected action " + _action);
	}
	
	@Override
	public String toString()
	{
		if (_action == Action.SLEEP)
		{
			return _action + " " + _ms + " ms";
		}
		else
		if (_action == Action.RENAME)
		{
			return _action + " " + _path + " -> " + _path2;
		}
		else
		{
			return _action + " " + _path;
		}
	}
}