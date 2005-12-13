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
		DELETE, CREATE, RENAME, MODIFY
	}
	
	private Command.Action _action;
	private String _path;
	private String _path2;

	public static Command delete(String path)
	{
		return new Command(Action.DELETE, path, null);
	}

	public static Command modify(String path)
	{
		return new Command(Action.MODIFY, path, null);
	}
	
	public static Command create(String path)
	{
		return new Command(Action.CREATE, path, null);
	}
	
	public static Command rename(String from, String to)
	{
		return new Command(Action.DELETE, from, to);
	}
	
	private Command(Command.Action action, String path, String path2)
	{
		_action = action;
		_path = path;
		_path2 = path2;
	}
	
	public boolean perform(File root) throws IOException
	{
		File file = new File(root, _path);
		if (_action == Action.CREATE)
		{
			System.err.println("Creating " + file);
			if (file.isDirectory())
			{
				return file.mkdir();
			}
			else
			{
				return file.createNewFile();
			}
		}
		else
		if (_action == Action.DELETE)
		{
			System.err.println("Deleting " + file);
			return file.delete();
		}
		else
		if (_action == Action.MODIFY)
		{
			System.err.println("Modifying " + file);
			FileOutputStream out = new FileOutputStream(file);
			try
			{
				out.write("A".getBytes());
			}
			finally
			{
				out.close();
			}
			return true;
			
		}
		else
		if (_action == Action.RENAME)
		{
			System.err.println("Renaming " + file + " -> " + _path2);
			return file.renameTo(new File(root, _path2));
		}
		// Unexpected action 
		throw new RuntimeException("Unexpected action " + _action);
	}
	
	@Override
	public String toString()
	{
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