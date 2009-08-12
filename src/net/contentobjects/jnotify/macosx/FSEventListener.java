package net.contentobjects.jnotify.macosx;

public interface FSEventListener
{
	public void notifyChange(int wd, String rootPath, String filePath);
}
