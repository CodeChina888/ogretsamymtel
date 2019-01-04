@echo off

rem # ������������
set gcDir=./log4j2/gc
set errDir=./log4j2/error
set srvName=srvWorld0
set runParam=0

title ��Ϸ��%srvName%

rem # ���ü�����Ļ������� ����������
set CPATH=./assets/;./config/;./libs/;./libs/*
set MAIN=game.seam.main.WorldStartup

rem # �������в���
set javaParam=-server -Xms512m -Xmx512m -Xmn192m -XX:MaxDirectMemorySize=128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m
set javaParam=%javaParam% -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+AlwaysLockClassLoader
set javaParam=%javaParam% -XX:+DisableExplicitGC
set javaParam=%javaParam% -XX:+PrintGCDateStamps -XX:+PrintGCDetails -verbose:gc -Xloggc:%gcDir%/%srvName%_gc.log
set javaParam=%javaParam% -XX:ErrorFile=%gcDir%/%srvName%_error_%p.log -XX:HeapDumpPath=%gcDir%/%srvName%_heapDump_%p.hprof
set javaParam=%javaParam% -Dsun.zip.disableMemoryMapping=true
set javaParam=%javaParam% -javaagent:libs\\classReloader.jar 

echo ===============================================================================
echo.
echo   %srvName% Environment
echo.
echo   javaParam: %runParam% %MAIN% %CPATH% %javaParam%
echo.
echo ===============================================================================
echo.

java %javaParam% ^
	-cp %CPATH% %MAIN% %runParam%
pause