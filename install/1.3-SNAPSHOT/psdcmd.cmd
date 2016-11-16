@echo off

SET VERSION=1.3
java -Dfile.encoding=UTF-8 -classpath jars\psdcmd-%VERSION%-SNAPSHOT.jar;jars\psd-image-2.1-SNAPSHOT.jar;jars\psd-parser-2.1-SNAPSHOT.jar;jars\psd-tool-2.1-SNAPSHOT.jar  com.visiansystems.psdcmd.PsdCmd %*

