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

		commands.add(Command.create("test"));
		commands.add(Command.create("test/2"));
		events.add(Event.created("test"));
		performTest(IJNotify.FILE_CREATED, false, commands, events);
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
		JNotify.get().addWatch(testRoot.getName(), mask, watchSubtree, new JNotifyListener()
		{

			public void fileRenamed(int wd, String rootPath, String oldName, String newName)
			{
				events.add(new Event(Event.ActionEnum.RENAMED, wd, rootPath, oldName, newName));
			}

			public void fileModified(int wd, String rootPath, String name)
			{
				events.add(new Event(Event.ActionEnum.MODIFIED, wd, rootPath, name));
			}

			public void fileDeleted(int wd, String rootPath, String name)
			{
				events.add(new Event(Event.ActionEnum.DELETED, wd, rootPath, name));
			}

			public void fileCreated(int wd, String rootPath, String name)
			{
				events.add(new Event(Event.ActionEnum.CREATED, wd, rootPath, name));
			}
		});

		for (int i = 0; i < commands.size(); i++)
		{
			Command command = commands.get(i);
			try
			{
				command.perform(testRoot);
			}
			catch (IOException e)
			{
				System.out.println("Error performing:");
				System.out.println(command);
				e.printStackTrace();
			}
		}

		try
		{
			Thread.sleep(2000);
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
		}
		else
		{
			file.delete();
		}
	}

}
