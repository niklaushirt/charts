#!/bin/bash

export SERVER_ADDR="9.30.250.6"
export SERVER_PORT_UI_HTTPS="9999"
export SERVER_PORT_UI_HTTP="8888"
export SERVER_PORT_JMS="7918"
export SERVER_PORT_WSS="7919"
export SERVER_PASSWORD="admin"
export SERVER_PATH=/opt/ibm-ucd/servertest

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo " Server will be configured as follows "
echo "-----------------------------------------------------------------------------"
echo "UCD Server Address    : "$SERVER_ADDR
echo "UCD Server UI Port    : "$SERVER_PORT_UI
echo "UCD Server JMS Port   : "$SERVER_PORT_JMS
echo "UCD Server WSS Port   : "$SERVER_PORT_WSS
echo "UCD Server Directory  : "$SERVER_PATH

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
rm -r $SERVER_PATH
rm -r ~/tmp/ucds-install/ibm-ucd-install


mkdir -p ~/tmp/ucds-install
cd ~/tmp/ucds-install

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo " DOWNLOAD STUFF "
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
#wget https://www.dropbox.com/s/dx873w7ioatxxnk/server.install.properties
#wget https://www.dropbox.com/sh/fz9o2gsu8rdtqy3/AABk_3Vw16BVk0NLZyj_9NLva?dl=0 -O plugins.zip
#wget https://www.dropbox.com/s/2t00hzfn7p694av/URBANCODE_DEPLOY_V7.0.0_MP_ML.zip -O UCD.zip
cp ~/server.install.properties ~/tmp/ucds-install/install.properties
cp ~/URBANCODE_DEPLOY_V7.0.0_MP_ML.zip ~/tmp/ucds-install/UCD.zip
cp ~/plugins.zip ~/tmp/ucds-install/plugins.zip


echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"


#-----------------------------------------------------------------------------------------------------------------
# Download UCDS
#-----------------------------------------------------------------------------------------------------------------
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "Unzipping UCDS"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
unzip -q UCD.zip

echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "Copying Config"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
cp ~/tmp/ucds-install/install.properties ~/tmp/ucds-install/ibm-ucd-install/install.properties

echo "Adapting Config"
sed -i '/agentcomm.uri/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/agent.comm.uri/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/server.external.web.url/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/install.server.dir/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/server.initial.password/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/install.server.web.host/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/install.server.web.https.port/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/install.server.web.ip/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties
sed -i '/install.server.web.port/d' ~/tmp/ucds-install/ibm-ucd-install/install.properties



if [ $SERVER_ADDR ]; then
 echo "install.server.web.host="$SERVER_ADDR >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "install.server.web.host=ucd-server" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi
if [ $SERVER_ADDR ]; then
 echo "install.server.web.ip="$SERVER_ADDR >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "install.server.web.ip=ucd-server" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi
if [ $SERVER_PORT_UI_HTTPS ]; then
 echo "install.server.web.https.port="$SERVER_PORT_UI_HTTPS >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "install.server.web.https.port=8443" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi
if [ $SERVER_PORT_UI_HTTP ]; then
 echo "install.server.web.http.port="$SERVER_PORT_UI_HTTP >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "install.server.web.http.port=8080" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi

if [ $SERVER_PORT_WSS ]; then
 echo "agentcomm.uri=wss://"$SERVER_ADDR":"$SERVER_PORT_WSS >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "agentcomm.uri=wss://ucd-server:7919" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi

if [ $SERVER_PORT_WSS ]; then
 echo "agent.comm.uri=wss://"$SERVER_ADDR":"$SERVER_PORT_WSS >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "agent.comm.uri=wss://ucd-server:7919" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi

if [ $SERVER_PATH ]; then
 echo "install.server.dir="$SERVER_PATH >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "install.server.dir=/opt/ibm-ucd/server" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi

if [ $SERVER_PASSWORD ]; then
 echo "server.initial.password="$SERVER_PASSWORD >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "server.initial.password=admin" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi


if [ $SERVER_PORT_WSS ]; then
 echo "agent.comm.uri=wss://"$SERVER_ADDR":"$SERVER_PORT_HTTPS >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
else
 echo "agent.comm.uri=wss://ucd-server:7919" >> ~/tmp/ucds-install/ibm-ucd-install/install.properties
fi




echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "INSTALL UCDS"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
cd ~/tmp/ucds-install/ibm-ucd-install

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre/
./install-server.sh

rm -r mkdir ~/tmp/ucds-install/plugins
mkdir ~/tmp/ucds-install/plugins
unzip -o ~/tmp/ucds-install/plugins.zip -d ~/tmp/ucds-install/plugins
mkdir -p $SERVER_PATH/appdata/var/plugins/command/stage
cp ~/tmp/ucds-install/plugins/* $SERVER_PATH/appdata/var/plugins/command/stage


echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "INSTALLED"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "STARTING SERVER"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"


# 	rm -rf /tmp/ibm-ucd-install
# 	rm -f /tmp/install.properties
$SERVER_PATH/bin/server start

tail -f $SERVER_PATH/var/log/stdout


echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "DONE"
echo "-----------------------------------------------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------------------------------------------"
