#!/usr/bin/env bash
#
# Author : tang
# Date : 2020-02-12
#
#############################################
# !!!!!! Modify here please

APP_MAIN_CLASS="com.weishao.approval.ApprovalWorkflowApplication"

#############################################

APP_HOME="${BASH_SOURCE-$0}"
APP_HOME="$(dirname "${APP_HOME}")"
APP_HOME="$(cd "${APP_HOME}"; pwd)"
APP_HOME="$(cd "$(dirname ${APP_HOME})"; pwd)"
#echo "Base Directory:${APP_HOME}"

APP_BIN_PATH=$APP_HOME/bin
APP_LIB_PATH=$APP_HOME/lib
APP_CONF_PATH=$APP_HOME/conf

APP_PID_FILE="${APP_HOME}/run/${APP_MAIN_CLASS}.pid"
APP_RUN_LOG="${APP_HOME}/run/run_${APP_MAIN_CLASS}.log"

[ -d "${APP_HOME}/run" ] || mkdir -p "${APP_HOME}/run"
[ -d "${APP_HOME}/logs" ] || mkdir -p "${APP_HOME}/logs"
cd ${APP_HOME}

echo -n `date +'%Y-%m-%d %H:%M:%S'`             		>>${APP_RUN_LOG}
echo "---- Start service [${APP_MAIN_CLASS}] process. "	>>${APP_RUN_LOG}

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=$(which java)
fi

str=`file -L $JAVA | grep 64-bit`
if [ -n "$str" ]; then
	JAVA_OPTS="-server -Xms2048m -Xmx3072m -Xmn1024m -XX:SurvivorRatio=2 -Xss256k -XX:-UseAdaptiveSizePolicy -XX:MaxTenuringThreshold=15 -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseCMSInitiatingOccupancyOnly -XX:+HeapDumpOnOutOfMemoryError"
else
	JAVA_OPTS="-server -Xms1024m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MaxPermSize=128m "
fi

# JVMFLAGS JVM参数可以在这里设置
JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Dfile.encoding=UTF-8"

#把lib下的所有jar都加入到classpath中
CLASSPATH=$APP_CONF_PATH
for var in $APP_LIB_PATH/*.jar
do
	CLASSPATH="$var:$CLASSPATH"
done

# 进程存在时退出，防止同一程序多个进程运行
res=`ps aux|grep java|grep $APP_HOME|grep $APP_MAIN_CLASS|grep -v grep|awk '{print $2}'`
if [ -n "$res"  ]; then
        echo "$res program is already running"
        exit 1
fi

# 真正启动程序，并设置后台服务运行
nohup $JAVA -cp $CLASSPATH $JAVA_OPTS $APP_MAIN_CLASS >>${APP_RUN_LOG} 2>&1 &
#echo "$$JAVA -cp $CLASSPATH $JAVA_OPTS $APP_MAIN >>${APP_RUN_LOG} 2>&1 &"

RETVAL=$?
PID=$!

echo ${PID} >${APP_PID_FILE}
exit ${RETVAL}
