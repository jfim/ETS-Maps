#!/bin/sh

mvn install:install-file -DgroupId=com.kitfox.svg -DartifactId=svg-salamander -Dversion=1.0 -Dpackaging=jar -Dfile=./lib/svgSalamander.jar

