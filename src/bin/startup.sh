#!/bin/sh

JAVA_OPTS="-Xms512m -Xmx512m -Dfile.encoding=UTF-8"  
CLASSPATH=`echo ../libs/*.jar | sed "s/ /:/g"`  
java ${JAVA_OPTS} -cp ${CLASSPATH} cn.linjujia.web.main.App
