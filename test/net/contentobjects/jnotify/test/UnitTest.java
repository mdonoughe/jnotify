package net.contentobjects.jnotify.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class UnitTest extends TestCase
{
	
	static boolean _isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
	static boolean _isLinux = System.getProperty("os.name").toLowerCase().contains("linux");

	public UnitTest(String name)
	{
		super(name);
	}

	public void testFlat1() throws Exception
	{
		ArrayList<Command> commands = new ArrayList<Command>();
		ArrayList<Event> events = new ArrayList<Event>();

		// create a dir
		commands.add(Command.createDir("test"));
		
		// and a file in that dir
		// since we don't listen recuresvily, we should only get one event for this.
		commands.add(Command.createFile("test/2"));
		commands.add(Command.createFile("test/3"));
		
		
		events.add(Event.created("test"));
		

		performTest(JNotify.FILE_ANY, false, commands, events);
	}
	
	
	public void testDelete() throws Exception
	{
		ArrayList<Command> commands = new ArrayList<Command>();
		ArrayList<Event> events = new ArrayList<Event>();

		// create a dir
		commands.add(Command.createDir("test"));
		events.add(Event.created("test"));
		
		commands.add(Command.delete("test"));
		events.add(Event.deleted("test"));
		
		performTest(JNotify.FILE_ANY, false, commands, events);
	}
	
	
	public void _testFlat2() throws Exception
	{
		ArrayList<Command> commands = new ArrayList<Command>();
		ArrayList<Event> events = new ArrayList<Event>();

		// create a dir
		commands.add(Command.createDir("test"));
		// and a file in that dir
		commands.add(Command.createFile("test/2"));
		// since we don't listen recuresvily, we should only get one event for
		// this.
		events.add(Event.created("test"));
		// delete the inner file. this should generate no event
		commands.add(Command.delete("test/2"));
		// delete the dir
		commands.add(Command.delete("test"));
		// this should generate an event.
		events.add(Event.deleted("test"));
		// create another file
		commands.add(Command.createFile("1"));
		events.add(Event.created("1"));
		// modify it.
		commands.add(Command.modify("1"));
		events.add(Event.modified("1"));
		// rename it
		commands.add(Command.rename("1", "2"));
		events.add(Event.renamed("1", "2"));

		performTest(JNotify.FILE_ANY, false, commands, events);
	}

	
	
	public void _testRecursive() throws Exception
	{
		ArrayList<Command> commands = new ArrayList<Command>();
		ArrayList<Event> events = new ArrayList<Event>();

		commands.add(Command.createDir("a"));
		events.add(Event.created("a"));

		commands.add(Command.createDir("a/b"));
		events.add(Event.created("a/b"));

		commands.add(Command.createDir("a/c"));
		events.add(Event.created("a/c"));

		commands.add(Command.createSleep(300));

		commands.add(Command.createDir("a/c/d"));
		events.add(Event.created("a/c/d"));

		performTest(JNotify.FILE_ANY, true, commands, events);

	}
	
	void performTest(int mask, boolean watchSubtree, ArrayList<Command> commands,
		ArrayList<Event> expectedEvents) throws IOException
	{

		String rootDir = "$$$_TEST_$$$/";
		File testRoot = new File(rootDir).getAbsoluteFile();
		// make sure the dir is empty.
		deleteDirectory(testRoot);

		testRoot.mkdirs();
		int wd2 = -1;
		try
		{
			final ArrayList<Event> actualEvents = new ArrayList<Event>();
			wd2 = JNotify.addWatch(testRoot.getName(), mask, watchSubtree,
				new JNotifyListener()
				{

					public void fileRenamed(int wd, String rootPath, String oldName, String newName)
					{
						Event event = new Event(Event.ActionEnum.RENAMED, wd, rootPath, oldName,
							newName);
						System.out.println("JUnit : "+event);
						actualEvents.add(event);
					}

					public void fileModified(int wd, String rootPath, String name)
					{
						Event event = new Event(Event.ActionEnum.MODIFIED, wd, rootPath, name);
						System.out.println("JUnit : "+event);
						actualEvents.add(event);
					}

					public void fileDeleted(int wd, String rootPath, String name)
					{
						Event event = new Event(Event.ActionEnum.DELETED, wd, rootPath, name);
						System.out.println("JUnit : " + event);
						actualEvents.add(event);
					}

					public void fileCreated(int wd, String rootPath, String name)
					{
						Event event = new Event(Event.ActionEnum.CREATED, wd, rootPath, name);
						System.out.println("JUnit : " + event);
						actualEvents.add(event);
					}
				});

			
			System.out.println("JUnit : Executing commands...");
			for (int i = 0; i < commands.size(); i++)
			{
				Command command = commands.get(i);
				try
				{
					System.out.println("JUnit : Action " + command);
					boolean perform = command.perform(testRoot);
					assertTrue("Error performing command " + command, perform);
				}
				catch (IOException e)
				{
					System.out.println("JUnit : Error performing:");
					System.out.println(command);
					e.printStackTrace();
					throw e;
				}
			}
			
			System.out.println("JUnit : Done, waiting for events to settle...");
			
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e1)
			{
				// nop
			}

			System.out.println("JUnit : Done, analyzing events");
			
			// actual events may be more then expected ,because on windows we 
			// get mofigied for deletions as well.
			assertTrue(expectedEvents.size() <= actualEvents.size(),);
			int expectedIndex=0,actualIndex=0;
			for (;expectedIndex < expectedEvents.size();)
			{
				Event expected = expectedEvents.get(expectedIndex);
				Event actual = actualEvents.get(actualIndex);
				
				// On windows, the sysetm sends both modified and deleted
				// in response to file deletion.
				// skip modified.
				if (_isWindows && expected.isDeleted() && actual.isModified())
				{
					// skip actual event
					actualIndex++;
					continue;
				}
				
				actualIndex++;
				expectedIndex++;
				
				assertMatch(expected, actual);
			}
		}
		catch(Error e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			System.out.println("JUnit : Removing watch " + wd2);
			boolean res = JNotify.removeWatch(wd2);
			if (!res)
			{
				System.err.println("JUnit: Warning, failed to remove watch");
			}
			System.out.println("JUnit : Deleting directory " + testRoot);
			deleteDirectory(testRoot);
		}
	}

	public void _testRemoveWatch1() throws JNotifyException
	{
		int wd = JNotify.addWatch(".",JNotify.FILE_ANY, false, new JNotifyAdapter());
		boolean removeWatch = JNotify.removeWatch(wd);
		assertTrue(removeWatch);
	}

	
	public void _testRemoveWatch2() throws IOException
	{
		ArrayList<Command> commands = new ArrayList<Command>();
		ArrayList<Event> events = new ArrayList<Event>();

		commands.add(Command.createDir("a"));
		events.add(Event.created("a"));

		commands.add(Command.createDir("a/b"));
		events.add(Event.created("a/b"));

		commands.add(Command.createDir("a/c"));
		events.add(Event.created("a/c"));

		commands.add(Command.createSleep(300));

		commands.add(Command.createDir("a/c/d"));
		events.add(Event.created("a/c/d"));
		

		performTest(JNotify.FILE_ANY, true, commands, events);
	}

	
	
	
	private void assertMatch(Event expected, Event actual)
	{
		assertEquals(expected.getAction(), actual.getAction());
		assertEquals(normalizePath(expected.getName()), normalizePath(actual.getName()));
		assertEquals(normalizePath(expected.getName2()), normalizePath(actual.getName2()));
	}

	static void deleteDirectory(File file)
	{
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++)
		{
			if (file.isDirectory())
			{
				deleteDirectory(files[i]);
			}
		}
		file.delete();
	}
	
	static String normalizePath(String path)
	{
		if (path == null)
		{
			return null;
		}
		
		StringBuffer sb = new StringBuffer(path);
		for (int i = 0; i < sb.length(); i++)
		{
			char c = sb.charAt(i);
			if (c == '/' || c == '\\')
			{
				sb.setCharAt(i, File.separatorChar);
			}
		}
		return sb.toString();
	}

}

