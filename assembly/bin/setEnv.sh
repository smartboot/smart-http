#!/bin/sh
HTTP_HOME=$(dirname $(pwd))
JAVA_OPTS=" -Duser.dir=${HTTP_HOME} -Dlog4j.configurationFile=file:${HTTP_HOME}/conf/log4j2.xml -Dhttp_config=${HTTP_HOME}/conf/config.properties -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${HTTP_HOME}/lib/ "