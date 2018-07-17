@echo off
REM Licensed Materials - Property of IBM* and/or HCL**
REM UrbanCode Deploy
REM UrbanCode Build
REM UrbanCode Release
REM AnthillPro
REM (c) Copyright IBM Corporation 2011, 2017. All Rights Reserved.
REM (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
REM
REM U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
REM GSA ADP Schedule Contract with IBM Corp.
REM
REM * Trademark of International Business Machines
REM ** Trademark of HCL Technologies Limited

setlocal

cd %~dp0

set ANT_HOME=opt\apache-ant-1.8.4
set ANT_OPTS=
set TYPE=
set INSTALL_TYPE=


:argloop
    set HANDLED=
    if "%1"=="" goto endargloop
    if "%1"=="-fips" (
        set ANT_OPTS=%ANT_OPTS% -Dcom.ibm.jsse2.usefipsprovider=true
        set HANDLED=yes
    )
    if "%1"=="--fips" (
        set HANDLED=yes
        set ANT_OPTS=%ANT_OPTS% -Dcom.ibm.jsse2.usefipsprovider=true
    )
    if "%1"=="-binary" (
        set HANDLED=yes
        set TYPE=binary
        call :setinstalltype
    )
    if "%1"=="--binary" (
        set HANDLED=yes
        set TYPE=binary
        call :setinstalltype
    )
    if "%1"=="-config" (
        set HANDLED=yes
        set TYPE=config
        call :setinstalltype
    )
    if "%1"=="--config" (
        set HANDLED=yes
        set TYPE=config
        call :setinstalltype
    )
    if "%ERRORLEVEL%"=="1" (
        call :printUsage
        goto :fail
    )
    if "%HANDLED%"=="yes" (
        shift
        goto argloop
    )
    echo "Unknown Argument: %1"
    call :printUsage
    goto :fail

:endargloop

%ANT_HOME%\bin\ant.bat -nouserlib -noclasspath -f install.with.groovy.xml install-agent

goto :EOF

:printUsage
    echo Usage: install-agent.bat [--fips OR -fips] [--binary OR -binary OR --config OR -config]
    exit /B 0

:setinstalltype
   if NOT "%INSTALL_TYPE%"=="" (
       echo Options binary or config can only be set once
       exit /B 1
   )
   set INSTALL_TYPE="%type%"
   set ANT_OPTS=%ANT_OPTS% -Dagent.install.type=%TYPE%
   exit /B 0

:fail
   exit /B 1
