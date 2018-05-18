CHCP 65001

@echo off & setlocal enabledelayedexpansion

set LIB_JARS=""
cd ..\lib
for %%i in (*) do set LIB_JARS=!LIB_JARS!;..\lib\%%i
cd ..\bin

set APP_MAIN_CLASS=com.iflytek.edu.kylin.command.RunCommand
set JAVA_MEM_OPTS=-Xms64m -Xmx1024m -XX:MaxPermSize=64M

set KYLIN_bak_single=backups:single
set KYLIN_bak_batch=backups:batch

set KYLIN_import_single=import:single
set KYLIN_import_batch=import:batch

set KYLIN_config_bak=../conf/kylin-backups.properties
set KYLIN_config_import=../conf/kylin-import.properties

if "%1" == "1" goto bakSingle
if "%1" == "1b" goto bakBatch
if "%1" == "2" goto importSingle
if "%1" == "2b" goto importBatch


:bakSingle
java %JAVA_MEM_OPTS% -classpath ..\conf;%LIB_JARS% %APP_MAIN_CLASS% -command=%KYLIN_bak_single% -config=%KYLIN_config_bak%
goto end

:bakBatch
java %JAVA_MEM_OPTS% -classpath ..\conf;%LIB_JARS% %APP_MAIN_CLASS% -command=%KYLIN_bak_batch% -config=%KYLIN_config_bak%
goto end

:importSingle
java %JAVA_MEM_OPTS% -classpath ..\conf;%LIB_JARS% %APP_MAIN_CLASS% -command=%KYLIN_import_single% -config=%KYLIN_config_import%
goto end

:importBatch
java %JAVA_MEM_OPTS% -classpath ..\conf;%LIB_JARS% %APP_MAIN_CLASS% -command=%KYLIN_import_batch% -config=%KYLIN_config_import%

:end
pause