@echo off

SET VERSION=1.2
SET LIBS=lib
java -Dfile.encoding=UTF-8 -classpath target\psdcmd-%VERSION%-SNAPSHOT.jar;%LIBS%\psd-image-2.0-SNAPSHOT.jar;%LIBS%\psd-parser-2.0-SNAPSHOT.jar;%LIBS%\psd-tool-2.0-SNAPSHOT.jar;%LIBS%\gson-2.7.jar  com.visiansystems.psdcmd.PsdCmd %*

