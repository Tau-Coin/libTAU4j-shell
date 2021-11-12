#!/bin/bash
currDir=$(pwd)
echo $currDir

classpath=$currDir/libs/libTAU4j-0.0.0.jar:$currDir/build/classes/java/main:$currDir/libs/core-1.51.0.0.jar
echo $classpath

libpath="java.library.path=$currDir/libs"
echo $libpath

mainclass=io.tau.DhtShell

java -cp $classpath -D$libpath $mainclass
