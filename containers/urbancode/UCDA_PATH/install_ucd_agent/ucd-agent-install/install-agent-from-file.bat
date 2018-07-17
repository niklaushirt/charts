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

REM ###########################################################################
REM This script performs a unattended agent installation using a property
REM file that contains the necessary installation properties.
REM The script requires one argument which is the path to the property file.
REM ###########################################################################

cd %~dp0

set ANT_HOME=opt\apache-ant-1.8.4

rem strip quotes from script argument
set propsfile=%1
set propsfile=###%1###
set propsfile=%propsfile:"###=%
set propsfile=%propsfile:###"=%
set propsfile=%propsfile:###=%

CALL opt\apache-ant-1.8.4\bin\ant.bat -f install.with.groovy.xml ^
     "-nouserlib" ^
     "-noclasspath" ^
     "-Dinstall-agent=true" ^
     "-Dinstall.properties.file=%propsfile%" ^
     install-non-interactive
