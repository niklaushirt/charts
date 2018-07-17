#!/bin/sh
# Licensed Materials - Property of IBM* and/or HCL**
# UrbanCode Deploy
# UrbanCode Build
# UrbanCode Release
# AnthillPro
# (c) Copyright IBM Corporation 2011, 2017. All Rights Reserved.
# (c) Copyright HCL Technologies Ltd. 2018. All Rights Reserved.
#
# U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
# GSA ADP Schedule Contract with IBM Corp.
#
# * Trademark of International Business Machines
# ** Trademark of HCL Technologies Limited
################################################################################
# This script performs a unattended agent installation using a property
# file that contains the necessary installation properties.
# The script requires one argument which is the path to the property file.
################################################################################

SHELL_NAME=$0
SHELL_PATH=`dirname ${SHELL_NAME}`

if [ "." = "$SHELL_PATH" ] 
then
    SHELL_PATH=`pwd`
fi
cd ${SHELL_PATH}

ANT_HOME=opt/apache-ant-1.8.4
export ANT_HOME

chmod -f +x "opt/apache-ant-1.8.4/bin/ant"

# Run the installation.
opt/apache-ant-1.8.4/bin/ant -f install.with.groovy.xml \
    "-nouserlib" \
    "-noclasspath" \
    "-Dinstall-agent=true" \
    "-Dinstall.properties.file=$1" \
    install-non-interactive
