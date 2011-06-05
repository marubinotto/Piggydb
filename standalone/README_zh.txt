Piggydb 独立程序包 - http://piggydb.net/
-----------------------------------------------------------------------
■ Piggydb独立程序包简介

这个软件包是把网上应用程序Piggydb(http://piggydb.net/)作为一个独立软件
来使用，不需要繁琐的网站安装。

■ 前提条件

使用这个软件，需要Java的运行环境（Java Runtime 1.6 以上）

你可以在这下载Java的运行环境

   http://www.java.com/download
   
因为没有在所有的OS环境上测试过，所以不能断言（Windows XP和Mac OS X
已确认可以运行），但只要有Java的运行环境和GUI系统托盘的OS环境，
应该可以使用这个独立程序包。

■ 安装

只需要解压Zip文件。

■ 启动Piggydb服務器

Windows
  
  双击piggydb.exe文档，服務器就会开始启动。
  
其他的OS
  
  双击piggydb-standalone.jar，服務器就会开始启动。
  如果不能启动的话，可以尝试从命令行［java -jar piggydb-standalone.jar］
  来启动。

当服务器启动好，会自动打开浏览器显示piggydb的注册页或首页。
在系统托盘上会显示piggydb的图标。

如果浏览器没有自动打开，请自己打开浏览器，访问 http://localhost:8080 。
首次使用这软件时，请用“owner/owner”来注册。进入首页后，
可以选择［控制台/变更密码］的菜单来变更密码。

如果服务器端口8080已被其他的系统使用，请编辑launcher.properties，
更改服务器端口，再从新启动服务器。

■ 使用方法

Piggydb的服务器启动后，在系统托盘上会显示piggydb的图标。
右击piggydb的图标（windows），会显示菜单。

数据库文件最初是在“~/piggydb/”（如果是Windows，
“C:\Documents and Settings\<windows用户名>\piggydb”）目录下创建。
数据库文件的位置，可以编辑application.properties文件来更改。
  
■ 停止Piggydb服务器

右击系统托盘上的piggydb的图标，选择菜单的［关闭］，服务器就会停止。

-----------------------------------------------------------------------
欢迎您的意見和感想。
请联系 daisuke.marubinotto@gmail.com 
