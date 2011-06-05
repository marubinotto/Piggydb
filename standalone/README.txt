Piggydb Standalone Package - http://piggydb.net/
-----------------------------------------------------------------------

1. REQUIREMENTS

Java 1.6 or later.
java.exe (Windows) or java (other OS's) must be in the system path.

Java Download: http://www.java.com/download

This package can run on any OS as long as Java and the system tray 
are available.

The system tray is referred to as:
  "Taskbar Status Area" on Windows
  "Menu Extras" on Mac OS X 
  "Notification Area" on Gnome 
  "System Tray" on KDE  

2. INSTALLATION

Unzip the distribution in a suitable location.

3. RUNNING PIGGYDB

Windows:
  Double-click piggydb.exe
  
Other Operating Systems:
  Double-click piggydb-standalone.jar
  or execute "java -jar piggydb-standalone.jar"

If port 8080 is already in use, modify launcher.properties to change 
the value of "port".

4. USAGE

When the Piggydb server is running, a Piggydb icon is displayed in the 
system tray. You can control the server via this icon.

To browse the home page, click "Open the home page" in the right-click 
menu of the system tray icon, or bring up a web browser and go to URL 
http://localhost:8080 (or http://localhost:port if not using the default).

Piggydb has only one user ("owner") by default, and the default password 
is "owner", so you can login with owner/owner. You should change owner's 
password immediately after the first login (Menu [System/Change Password]). 

The database files will be created in ~/piggydb/ by default 
(or in C:\Documents and Settings\<User>\piggydb if you use Windows); 
this allows your data to be reused when you upgrade to a later version of Piggydb.

You can change the location of the database files by modifying 
application.properties.

5. STOPPING PIGGYDB

Click "Shutdown" in the menu of the system tray icon.

-----------------------------------------------------------------------
You can contribute to the development of Piggydb by sending feedback or 
bug reports, feature requirements via email 
(daisuke.marubinotto (at) gmail.com)

Grow your own knowledge base! 
