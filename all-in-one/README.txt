Piggydb - http://piggydb.net/
-----------------------------------------------------------------------

1. REQUIREMENTS

Java 1.6 or later.
java.exe (Windows) or java (other OS's) must be in the system path.

Java Download: http://www.java.com/

2. INSTALLATION

Unzip the distribution in a suitable location.

3. RUNNING PIGGYDB

Windows:
  Double-click run.bat.
Unix, Linux, Mac OS X:
  Run run.sh.

If port 8080 is already in use, modify winstone.properties to change 
the value of "httpPort".

4. USAGE

If the Piggydb server is running on the local machine, simply bring up 
a web browser and go to URL http://localhost:8080 or
http://localhost:number, if not using port 8080.

Piggydb has only one user ("owner") by default, and the default password 
is "owner", so you can login with owner/owner. You should change owner's 
password immediately after the first login (Menu [System/Change Password]). 

The database files will be created in ~/piggydb/ by default 
(or in C:\Documents and Settings\<User>\piggydb if you use Windows); 
this allows your data to be reused when you upgrade to a later version of Piggydb.

You can change the location of the database files by modifying run.bat
or run.sh. If you use Windows and you want to change the database 
location to C:\data and use "sample" as a database name, then change
the default location ~/piggydb/piggydb in run.bat or run.sh to 
file:C:/data/sample  

5. STOPPING PIGGYDB

Kill the process (either by Ctrl-C or kill command). 

-----------------------------------------------------------------------
You can contribute to the development of Piggydb by sending feedback or 
bug reports, feature requirements via email 
(daisuke.marubinotto (at) gmail.com)

Grow your own knowledge base! 
