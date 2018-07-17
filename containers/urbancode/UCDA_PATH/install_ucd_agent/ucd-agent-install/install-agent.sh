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

# save current state
PREVIOUS_DIR=`pwd`
PREVIOUS_ANT_HOME=$ANT_HOME

# now change the dir to the root of install script
SHELL_NAME=$0
SHELL_PATH=`dirname ${SHELL_NAME}`


if [ "." = "$SHELL_PATH" ]
then
   SHELL_PATH=`pwd`
fi
cd "${SHELL_PATH}"

# set ANT_HOME
ANT_HOME=opt/apache-ant-1.8.4
export ANT_HOME

ANT_OPTS=

INSTALL_TYPE=

SPECIFY="Can only specify config or binary once"
USAGE="install-agent.sh [--fips|-fips] [--binary|-binary|--config|-config]";

while [ "$#" -gt 0 ] ;
do
    ARG="${1}"
    shift;
    case "${ARG}" in
        '--fips' | '-fips')
            ANT_OPTS="${ANT_OPTS} -Dcom.ibm.jsse2.usefipsprovider=true";
            ;;
        '--binary' | '-binary')
                if [ -n "${INSTALL_TYPE}" ]
                then
                    echo $SPECIFY
                    echo $USAGE;
                    exit 1;
                else
                   INSTALL_TYPE="$1"
                   ANT_OPTS="${ANT_OPTS} -Dagent.install.type=$1"
                fi
            ;;
        '--config' | '-config')
                if [ -n "${INSTALL_TYPE}" ]
                then
                    echo $SPECIFY
                    echo $USAGE;
                    exit 1;
                else
                   INSTALL_TYPE="$1"
                   ANT_OPTS="${ANT_OPTS} -Dagent.install.type=$1"
                fi
            ;;
        *)
            echo "Unknown Argument: ${ARG}";
            echo $USAGE;
            exit 1;
            ;;
    esac
done
export ANT_OPTS

chmod -f +x "opt/apache-ant-1.8.4/bin/ant"

# run the install
opt/apache-ant-1.8.4/bin/ant -nouserlib -noclasspath -f install.with.groovy.xml install-agent

# restore previous state
cd "${PREVIOUS_DIR}"
ANT_HOME=${PREVIOUS_ANT_HOME}
export ANT_HOME
