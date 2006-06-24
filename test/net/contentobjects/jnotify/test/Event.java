/**
 * 
 */
package net.contentobjects.jnotify.test;

class Event
{

	public static final String DELETED = "DELETED";
	public static final String CREATED = "CREATED";
	public static final String RENAMED = "RENAMED";
	public static final String MODIFIED = "MODIFIED";

	private final String _action;
	private final int _wd;
	private final String _path;
	private final String _name;
	private final String _name2;

	public static Event deleted(String path)
	{
		return new Event(DELETED, -1, "UNKNOWN", path, null);
	}

	public static Event modified(String path)
	{
		return new Event(MODIFIED, -1, "UNKNOWN", path, null);
	}

	public static Event created(String path)
	{
		return new Event(CREATED, -1, "UNKNOWN", path, null);
	}

	public static Event renamed(String from, String to)
	{
		return new Event(RENAMED, -1, "UNKNOWN", from, to);
	}

	Event(String action, int wd, String path, String name)
	{
		this(action, wd, path, name, null);
	}

	Event(String action, int wd, String path, String name, String name2)
	{
		_action = action;
		_wd = wd;
		_path = path;
		_name = name;
		_name2 = name2;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof Event)
		{
			Event other = (Event) obj;
			return other._action == _action && other._name.equals(_name)
					&& other._path.equals(_path) && (_wd == -1 || other._wd == _wd);
		}
		return false;
	}

	public String toString()
	{
		return "Event : " + _action + " wd=" + _wd + ", path=" + _path + " name=" + _name
				+ (_name2 != null ? ", name2=" + _name2 : "");
	}

	public String getAction()
	{
		return _action;
	}

	public String getName()
	{
		return _name;
	}

	public String getName2()
	{
		return _name2;
	}

	public String getPath()
	{
		return _path;
	}

	public int getWd()
	{
		return _wd;
	}

	public boolean isCreated()
	{
		return getAction() == CREATED;
	}

	public boolean isRenamed()
	{
		return getAction() == RENAMED;
	}

	public boolean isDeleted()
	{
		return getAction() == DELETED;
	}

	public boolean isModified()
	{
		return getAction() == MODIFIED;
	}
}
