JNotify works on Linux with INotify support (Tested on 2.6.14), Mac OS X 10.5 or higher (Tested on 10.5.8), and Windows XP/2K/NT (Tested on XP).

Because of the way Mac OS X handles file change notifications, some things may work differently. Other platforms still work the same way as before, but if you want your code to work everywhere, you should be aware of the following:

  * Events are not guaranteed to be reported in order. Events should be reported within two seconds of occuring.
  * File changes that are present less than two seconds may be missed. For example, `mv a b && mv b c` may report only a single rename.
  * The code that detects renames is only aware of files being watched. If a file outside the watched directory is moved into the directory, it will be reported as a create. Likewise, if a file is moved out of the watched directory, it will be reported as a delete.

To use JNotify, You will need to have the appropriate shared library in your java.library.path

Usage is very simple:

    // to add a watch : 
    String path = "/home/omry/tmp";
    int mask = JNotify.FILE_CREATED | 
               JNotify.FILE_DELETED | 
               JNotify.FILE_MODIFIED| 
               JNotify.FILE_RENAMED;
    boolean watchSubtree = true;
    int watchID = JNotify.addWatch(path, mask, watchSubtree, new JNotifyListener()
    {
      public void fileRenamed(int wd, String rootPath, String oldName,
          String newName)
      {
        System.out.println("JNotifyTest.fileRenamed() : wd #" + wd + " root = "
            + rootPath + ", " + oldName + " -> " + newName);
      }

      public void fileModified(int wd, String rootPath, String name)
      {
        System.out.println("JNotifyTest.fileModified() : wd #" + wd + " root = "
            + rootPath + ", " + name);
      }

      public void fileDeleted(int wd, String rootPath, String name)
      {
        System.out.println("JNotifyTest.fileDeleted() : wd #" + wd + " root = "
            + rootPath + ", " + name);
      }

      public void fileCreated(int wd, String rootPath, String name)
      {
        System.out.println("JNotifyTest.fileCreated() : wd #" + wd + " root = "
            + rootPath + ", " + name);
      }
    });

    // to remove watch:
    boolean res = JNotify.removeWatch(watchID);
    if (!res)
    {
      // invalid watch ID specified.
    }
