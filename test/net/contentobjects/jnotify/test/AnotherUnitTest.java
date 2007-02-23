package net.contentobjects.jnotify.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import net.contentobjects.jnotify.test.AnotherUnitTest.FileSystemEvent.FileSystemEventType;

/**
 * Unit test by Fabio Bernasconi
 */
public class AnotherUnitTest extends TestCase
{
    private static final String TEST_DIR = "test/JNotify";

    private Queue<FileSystemEvent> mEvents = new LinkedList<FileSystemEvent>();
    private List<Integer> mWatchIds = new LinkedList<Integer>();

    private File mRoot;

    /**
     * Renaming works always when moving/renaming files within one directory.
     */
    public void testRenameFlat1() throws Exception
    {
        setup(JNotify.FILE_ANY, false);

        {
            createFile("file1");
            createFile("file2");

            createDir("a");
            // no events for this
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            renameFile("file1", "file1_renamed");
            renameFile("file2", "a/file2_renamed");
            renameFile("a/file2_renamed", "file2_renamed");

            sleep();
        }

        assertEquals(5, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.RENAMED,
                "file1_renamed", "file1"), mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.RENAMED,
                "file2_renamed"), mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    /**
     * As soon as a file is renamed/moved into or from a watched subdir into an
     * other watched subdir <code>oldName</code> (in
     * {@link JNotifyListener#fileRenamed(int, String, String, String)}, third
     * parameter) is always null. It should be null only if it was moved from a
     * non watched directory.
     */
    public void testRenameRecursive1() throws Exception
    {
        setup(JNotify.FILE_ANY, true);

        {
            createFile("file1");
            createFile("file2");

            createDir("a");
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            renameFile("file1", "file1_renamed");

            // this operation fails before bugfix
            renameFile("file2", "a/file2");
            renameFile("a/file2", "file2");

            sleep();
        }

        assertEquals(7, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file3"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.RENAMED,
                "file1_renamed", "file1"), mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.RENAMED, "a/file2",
                "file2"), mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.RENAMED, "file2",
                "a/file2"), mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testCreateFlat1() throws Exception
    {
        setup(JNotify.FILE_ANY, false);

        {
            createDir("a");
            createFile("a/file1");
            createFile("a/file2");
            createFile("a/file3");
            sleep();
        }

        assertEquals(1, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testCreateRecursive1() throws Exception
    {
        setup(JNotify.FILE_ANY, true);

        {
            createDir("a");
            createFile("a/file1");
            createFile("a/file2");
            createFile("a/file3");
            sleep();
        }

        assertEquals(4, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file3"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testDeleteFlat1() throws Exception
    {
        setup(JNotify.FILE_ANY, false);

        {
            createFile("file1");

            // no events for this
            createDir("a");
            createFile("a/file2");
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            deleteFile("a/file2");
            deleteFile("a/file3");

            // this should create an event...
            deleteFile("file1");
            deleteDir("a");
            sleep();
        }

        assertEquals(4, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.DELETED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.DELETED, "a"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testDeleteRecursive1() throws Exception
    {
        setup(JNotify.FILE_ANY, true);

        {
            createFile("file1");

            createDir("a");
            createDir("a/b");

            createFile("a/file2");
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            deleteFile("a/file3");
            deleteFile("a/file2");
            deleteFile("a/b");
            deleteFile("a");

            deleteFile("file1");

            sleep();
        }

        assertEquals(10, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/b"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file3"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.DELETED, "a/file3"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.DELETED, "a/file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.DELETED, "a/b"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.DELETED, "a"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.DELETED, "file1"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testModifyFlat1() throws Exception
    {
        setup(JNotify.FILE_ANY, false);

        {
            createFile("file1");

            // no events for this
            createDir("a");
            createFile("a/file2");
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            modifyFile("a/file2");
            modifyFile("file1");

            sleep();
        }

        assertEquals(3, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.MODIFIED, "file1"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    public void testModifyRecursive1() throws Exception
    {
        setup(JNotify.FILE_ANY, true);

        {
            createFile("file1");

            // no events for this
            createDir("a");
            createFile("a/file2");
            createFile("a/file3");

            // make sure events were enqueued before operating
            // on the newly created files...
            sleep();

            modifyFile("a/file2");
            modifyFile("file1");

            sleep();
        }

        assertEquals(6, mEvents.size());

        assertEventEquals(createEvent(FileSystemEventType.CREATED, "file1"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.CREATED, "a/file3"),
                mEvents.poll());

        assertEventEquals(createEvent(FileSystemEventType.MODIFIED, "a/file2"),
                mEvents.poll());
        assertEventEquals(createEvent(FileSystemEventType.MODIFIED, "file1"),
                mEvents.poll());

        assertTrue(mEvents.isEmpty());
    }

    /* helper methods */
    private void assertEventEquals(FileSystemEvent expected,
            FileSystemEvent actual)
    {
        assertNotNull("Expected was null.", expected);
        assertNotNull("Actual was null.", actual);

        assertEquals(expected.getEventType(), actual.getEventType());
        assertEquals(expected.getPath(), actual.getPath());
        assertEquals(expected.getPathBefore(), actual.getPathBefore());
    }

    private FileSystemEvent createEvent(FileSystemEventType eventType,
            String path1)
    {
        return createEvent(eventType, path1, null);
    }

    private FileSystemEvent createEvent(FileSystemEventType eventType,
            String path1, String path2)
    {
        File p1 = new File(mRoot, path1);
        File p2 = path2 != null ? new File(mRoot, path2) : p1;

        return new FileSystemEvent(eventType, p1.getAbsolutePath(), p2
                .getAbsolutePath());
    }

    /**
     * Should manually be called in every test.
     * 
     * @param mask
     * @param watchSubtree
     * @throws Exception
     */
    private void setup(int mask, boolean watchSubtree)
    {
        mRoot = new File(TEST_DIR);

        // make sure test directory is empty
        deleteDir(mRoot);

        // clear members...
        mEvents.clear();
        mWatchIds.clear();

        assertTrue("Could not create root directory", mRoot.mkdirs());

        JNotifyWrapper notifyWrapper = new JNotifyWrapper();
        try
        {
            Integer addWatch = JNotify.addWatch(mRoot.getAbsolutePath(), mask,
                    watchSubtree, notifyWrapper);
            mWatchIds.add(addWatch);
        } catch (JNotifyException e)
        {
            fail(e.getMessage());
        }
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(1000);
        } catch (InterruptedException e)
        {
            fail(e.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        for (Integer watchId : mWatchIds)
            JNotify.removeWatch(watchId);

        deleteDir(mRoot);

        mWatchIds.clear();
        mEvents.clear();
    }

    private void createDir(String path)
    {
        File file = new File(mRoot, path);
        assertTrue("Could not create director(y|ies)", file.mkdirs());
    }

    private void createFile(String path)
    {
        File file = new File(mRoot, path);

        try
        {
            boolean created = file.createNewFile();
            assertTrue("Could not create file", created);
        } catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    private void renameFile(String oldName, String newName)
    {
        File oldFile = new File(mRoot, oldName);
        File newFile = new File(mRoot, newName);

        assertTrue("Could not rename file " + //
                oldFile.getAbsolutePath() + " to " + //
                newFile.getAbsolutePath(), //
                oldFile.renameTo(newFile) //
        );
    }

    private void modifyFile(String name)
    {
        File file = new File(mRoot, name);
        assertTrue("File either not existant or not writable.", file.isFile()
                && file.canWrite());

        try
        {
            FileWriter writer = new FileWriter(file);
            writer.write('a');
            writer.close();
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    private void deleteFile(String name)
    {
        File file = new File(mRoot, name);
        assertTrue("Could not delete file: " + name, file.delete());
    }

    private void deleteDir(String name)
    {
        File file = new File(mRoot, name);
        deleteDir(file);
    }

    /**
     * Deletes a directory and all its child nodes.
     */
    private void deleteDir(File root)
    {
        File[] listFiles = root.listFiles();
        if(listFiles != null)
        {
            for (File file : listFiles)
                deleteDir(file);
        }

        root.delete();
    }

    public static class FileSystemEvent
    {
        private final FileSystemEventType mEventType;
        private final String mPath;
        private final String mPathBefore;

        public FileSystemEvent(FileSystemEventType eventType, String path,
                String pathBefore)
        {
            mEventType = eventType;
            mPath = path;
            mPathBefore = pathBefore;
        }

        public FileSystemEventType getEventType()
        {
            return mEventType;
        }

        public String getPath()
        {
            return mPath;
        }

        public String getPathBefore()
        {
            return mPathBefore;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof FileSystemEvent)
            {
                FileSystemEvent other = (FileSystemEvent) obj;
                if(mEventType.equals(other.mEventType))
                {
                    boolean pathEquals = mPath.equals(other.mPath);
                    boolean pathBeforeEquals = mPathBefore
                            .equals(other.mPathBefore);

                    return pathEquals && pathBeforeEquals;
                }
            }
            return false;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[type:" + mEventType
                    + ",path:" + mPath + ",path before:" + mPathBefore + "]";
        }

        public static enum FileSystemEventType
        {
            CREATED, //
            DELETED, //
            RENAMED, //
            MODIFIED, //
            UNKNOWN; // 
        }
    }

    public class JNotifyWrapper implements JNotifyListener
    {
        private FileSystemEvent mLastEvent = new FileSystemEvent(
                FileSystemEventType.UNKNOWN, "", "");

        public void fileCreated(int wd, String rootPath, String name)
        {
            fireEvent(FileSystemEventType.CREATED, rootPath, name, null);
        }

        public void fileModified(int wd, String rootPath, String name)
        {
            fireEvent(FileSystemEventType.MODIFIED, rootPath, name, null);
        }

        public void fileRenamed(int wd, String rootPath, String oldName,
                String newName)
        {
            fireEvent(FileSystemEventType.RENAMED, rootPath, newName, oldName);
        }

        public void fileDeleted(int wd, String rootPath, String name)
        {
            fireEvent(FileSystemEventType.DELETED, rootPath, name, null);
        }

        protected void fireEvent(FileSystemEventType type, String rootPath,
                String pathPart1, String pathPart2)
        {
            // System.out.println("Type:" + type + ", root:" + rootPath + ",
            // old:"
            // + pathPart1 + ",new:" + pathPart2);

            String path1 = concatPath(rootPath, pathPart1);
            String path2 = pathPart2 != null ? concatPath(rootPath, pathPart2)
                    : path1;

            FileSystemEvent fileSystemEvent = new FileSystemEvent(type, path1,
                    path2);

            if(!mLastEvent.equals(fileSystemEvent))
                mEvents.offer(fileSystemEvent);

            mLastEvent = fileSystemEvent;
        }

        private String concatPath(String root, String xxx)
        {
            // TODO

            if(xxx != null && xxx.length() > 0)
            {
                if(xxx.endsWith("/") || xxx.endsWith("\\"))
                    xxx = xxx.substring(0, xxx.length() - 1);

                return root + "/" + xxx;
            }

            return root;
        }
    }
}
