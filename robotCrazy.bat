@echo off
title �����˷�����

rem # ������������ ��һ���Ƿ�����ip���ڶ����Ƕ˿� ��������������� ���ĸ��ƶ������˵����� 
set RUN_PARAM=%RUN_PARAM% 10.163.254.204
set RUN_PARAM=%RUN_PARAM% 10188
set RUN_PARAM=%RUN_PARAM% 1000
set RUN_PARAM=%RUN_PARAM% 1000
set RUN_PARAM=%RUN_PARAM% "name"

rem # ���ü�����Ļ������� ����������
set CPATH=./assets/;./config/;./libs/;./libs/*
set MAIN=game.robotCrazy.RobotCrazyStartup

rem # �������в���
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