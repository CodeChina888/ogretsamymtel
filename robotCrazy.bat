@echo off
title 机器人服务器

rem # 设置启动参数 第一个是服务器ip，第二个是端口 第三个是启动间隔 第四个移动机器人的数量 
set RUN_PARAM=%RUN_PARAM% 10.163.254.204
set RUN_PARAM=%RUN_PARAM% 10188
set RUN_PARAM=%RUN_PARAM% 1000
set RUN_PARAM=%RUN_PARAM% 1000
set RUN_PARAM=%RUN_PARAM% "name"

rem # 设置加载类的环境变量 运行主类名
set CPATH=./assets/;./config/;./libs/;./libs/*
set MAIN=game.robotCrazy.RobotCrazyStartup

rem # 设置运行参数
set JAVA_PARAMS=-server -Xms3g -Xmx3g -Xmn1g -Xss128k
set JAVA_PARAMS=%JAVA_PARAMS% -XX:+UseParallelGC -XX:+UseParallelOldGC
set JAVA_PARAMS=%JAVA_PARAMS% -XX:+DisableExplicitGC

echo ===============================================================================
echo.
echo   robot Environment
echo.
echo ===============================================================================
echo.

java -cp %CPATH% %MAIN% %RUN_PARAM% %JAVA_PARAMS%
pause