package net.contentobjects.jnotify.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;
import net.contentobjects.jnotify.IJNotify;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

public class UnitTest extends TestCase
{

	public UnitTest(String name)
	{
		super(name);
	}

	public void test1() throws Exception
	{
		ArrayList<Command> commands = new ArrayList();
		ArrayList<Event> events = new ArrayList();

		// create a dir
		commands.add(Command.createDir("test"));
		// and a file in that dir
		commands.add(Command.createFile("test/2"));
		// since we don't listen recuresvily, we should only get one event for this.
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
		commands.add(Command.rename("1","2"));
		events.add(Event.renamed("1","2"));
		
		performTest(IJNotify.FILE_ANY, false, commands, events);
	}

	void performTest(int mask, boolean watchSubtree, ArrayList<Command> commands,
		ArrayList<Event> extectedEvents) throws IOException
	{
		String rootDir = "$$$_TEST_$$$";
		
		File testRoot = new File(rootDir);
		// make sure the dir is empty.
		deleteDirectory(testRoot);
		
		testRoot.mkdirs();

		final ArrayList<Event> events = new ArrayList();
		int wd2 = JNotify.get().addWatch(testRoot.getName(), mask, watchSubtree, new JNotifyListener()
		{

			public void fileRenamed(int wd, String rootPath, String oldName, String newName)
			{
				Event event = new Event(Event.ActionEnum.RENAMED, wd, rootPath, oldName, newName);
				System.out.println(event);
				events.add(event);
			}

			public void fileModified(int wd, String rootPath, String name)
			{
				Event event = new Event(Event.ActionEnum.MODIFIED, wd, rootPath, name);
				System.out.println(event);
				events.add(event);
			}

			public void fileDeleted(int wd, String rootPath, String name)
			{
				Event event = new Event(Event.ActionEnum.DELETED, wd, rootPath, name);
				System.out.println(event);
				events.add(event);
			}

			public void fileCreated(int wd, String rootPath, String name)
			{
				Event event = new Event(Event.ActionEnum.CREATED, wd, rootPath, name);
				System.out.println(event);
				events.add(event);
			}
		});

		for (int i = 0; i < commands.size(); i++)
		{
			Command command = commands.get(i);
			try
			{
				boolean perform = command.perform(testRoot);
				assertTrue("Error performing command " + command, perform);
			}
			catch (IOException e)
			{
				System.out.println("Error performing:");
				System.out.println(command);
				e.printStackTrace();
				throw e;
			}
		}

		try
		{
			Thread.sleep(300);
		}
		catch (InterruptedException e1)
		{
			// nop
		}

		assertEquals(extectedEvents.size(), events.size());
		for (int i = 0; i < events.size(); i++)
		{
			assertMatch(extectedEvents.get(i),events.get(i));
		}
		
		JNotify.get().removeWatch(wd2);
		
		deleteDirectory(testRoot);

	}

	private void assertMatch(Event expected, Event actual)
	{
		assertEquals(expected.getAction(), actual.getAction());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getName2(), actual.getName2());
	}

	static void deleteDirectory(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			for (int i = 0; files != null && i < files.length; i++)
			{
				deleteDirectory(files[i]);
			}
			file.delete();
		}
		else
		{
			file.delete();
		}
	}

}
