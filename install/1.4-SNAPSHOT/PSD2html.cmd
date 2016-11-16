@echo off

SET VERSION=1.4
java -Dfile.encoding=UTF-8 -classpath jars\psdcmd-%VERSION%-SNAPSHOT.jar;jars\psd-image-2.2-SNAPSHOT.jar;jars\psd-parser-2.2-SNAPSHOT.jar;jars\psd-tool-2.2-SNAPSHOT.jar  com.visiansystems.psdcmd.PsdCmd %*

