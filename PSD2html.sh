#!/bin/bash

VERSION=1.3
LIBS=lib
java -Dfile.encoding=UTF-8 -classpath target/psdcmd-$VERSION-SNAPSHOT.jar:$LIBS/psd-image-2.1-SNAPSHOT.jar:$LIBS/psd-parser-2.1-SNAPSHOT.jar:$LIBS/psd-tool-2.1-SNAPSHOT.jar com.visiansystems.psdcmd.PsdCmd $*
