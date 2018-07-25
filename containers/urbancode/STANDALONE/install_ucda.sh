#!/bin/bash

export SERVER_ADDR="9.30.250.6"
export SERVER_PORT_UI_HTTPS="9999"
export SERVER_PORT_UI_HTTP="8888"
export SERVER_PORT_JMS="7918"
export SERVER_PORT_WSS="7919"
export AGENT_NAME="myagent"
export AGENT_PATH=/opt/ibm-ucd/agent

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo " Agent will be configured as follows "
echo "-----------------------------------------------------------------------------"
echo "UCD Server Address    : "$SERVER_ADDR
echo "UCD Server UI Port    : "$SERVER_PORT_UI
echo "UCD Server JMS Port   : "$SERVER_PORT_JMS
echo "UCD Server WSS Port   : "$SERVER_PORT_WSS
echo "UCD Agent Directory   : "$AGENT_PATH
echo "UCD Agent Name        : "$AGENT_NAME

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo " Trying to stop existin agent"
echo "-----------------------------------------------------------------------------"
$AGENT_PATH/bin/agent stop
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
rm -r $AGENT_PATH
rm -r ~/tmp/ucda-install/

mkdir -p ~/tmp/ucda-install
cd ~/tmp/ucda-install

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo " DOWNLOAD STUFF "
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
#wget https://www.dropbox.com/s/dx873w7ioatxxnk/agent.install.properties
wget https://$SERVER_ADDR:$SERVER_PORT_UI/tools/ucd-agent.zip --no-check-certificate

cp ~/agent.install.properties ~/tmp/ucda-install/agent.install.properties
#cp ~/URBANCODE_DEPLOY_V7.0.0_MP_ML.zip ~/tmp/ucda-install/UCD.zip
#cp ~/plugins.zip ~/tmp/ucda-install/plugins.zip


echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"


#-----------------------------------------------------------------------------------------------------------------
# Download UCDS
#-----------------------------------------------------------------------------------------------------------------
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "DOWNLOADING UCDS"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
unzip -q ucd-agent.zip

echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "DOWNLOADING Config"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"

cp ~/tmp/ucda-install/agent.install.properties ~/tmp/ucda-install/ucd-agent-install/agent.install.properties


sed -i '/locked\/agent.home/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/locked\/agent.jms.remote.host/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/locked\/agent.jms.remote.port/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/locked\/agent.name/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/locked\/agent.id/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/server.url/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
sed -i '/agentcomm.server.uri/d' ~/tmp/ucda-install/ucd-agent-install/agent.install.properties

export timestamp=$(date +%s)
agentName=$AGENT_NAME"-"$HOSTNAME"-"$timestamp

echo "locked/agent.home="$AGENT_PATH >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
echo "locked/agent.name="$agentName >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
echo "locked/agent.id="$agentName >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
echo "locked/agent.jms.remote.port="$SERVER_PORT_JMS >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
echo "locked/agent.jms.remote.host="$SERVER_ADDR >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties
echo "agentcomm.server.uri=wss://"$SERVER_ADDR":"$SERVER_PORT_WSS >> ~/tmp/ucda-install/ucd-agent-install/agent.install.properties

more ~/tmp/ucda-install/ucd-agent-install/agent.install.properties


echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "INSTALL UCDS"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
cd ~/tmp/ucda-install/ucd-agent-install

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/
./install-agent-from-file.sh agent.install.properties

echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "INSTALLED"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "STARTING AGENT"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"


# 	rm -rf /tmp/ibm-ucd-install
# 	rm -f /tmp/install.properties
$AGENT_PATH/bin/agent start

tail -f $AGENT_PATH/var/log/agent.out

echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "DONE"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
