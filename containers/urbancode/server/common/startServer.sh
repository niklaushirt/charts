#!/bin/sh
# (C) Copyright IBM Corporation 2016.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo "  Server will be configured as follows "
echo "-----------------------------------------------------------------------------"
echo "UCD Server Address      : "$SERVER_ADDR
echo "UCD Server UI Port      : "$SERVER_PORT_UI
echo "UCD Server JMS Port     : "$SERVER_PORT_JMS
echo "UCD Server HTTP Port    : "$SERVER_PORT_HTTP
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"


#Removing the lines from the file
sed -i '/agentcomm.uri/d' /opt/ibm-ucd/server/conf/server/installed.properties
sed -i '/agent.comm.uri/d' /opt/ibm-ucd/server/conf/server/installed.properties
sed -i '/server.external.web.ur/d' /opt/ibm-ucd/server/conf/server/installed.properties


echo "Copying Plugins files at $timestamp."
mkdir -p /opt/ibm-ucd/server/appdata/var/plugins/command/stage
cp /tmp/plugins/* /opt/ibm-ucd/server/appdata/var/plugins/command/stage


if [ $SERVER_ADDR ]; then
  echo "" >>/etc/hosts
  echo $SERVER_ADDR" ucd-server" >>/etc/hosts
  echo $SERVER_ADDR" ucd-server added to hosts"
fi

if [ $SERVER_PORT_UI ]; then
  echo "server.external.web.url=https://ucd-server:"$SERVER_PORT_UI >> /opt/ibm-ucd/server/conf/server/installed.properties
else
  echo "server.external.web.url=https://ucd-server:8443" >> /opt/ibm-ucd/server/conf/server/installed.properties
fi

if [ $SERVER_PORT_HTTP ]; then
  echo "agentcomm.uri=wss://ucd-server:"$SERVER_PORT_HTTP >> /opt/ibm-ucd/server/conf/server/installed.properties
else
  echo "agentcomm.uri=wss://ucd-server:7919" >> /opt/ibm-ucd/server/conf/server/installed.properties
fi

if [ $SERVER_PORT_HTTP ]; then
  echo "agent.comm.uri=wss://ucd-server:"$SERVER_PORT_HTTP >> /opt/ibm-ucd/server/conf/server/installed.properties
else
  echo "agent.comm.uri=wss://ucd-server:7919" >> /opt/ibm-ucd/server/conf/server/installed.properties
fi

echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"
echo "  installed.properties "
echo "-----------------------------------------------------------------------------"
cat /opt/ibm-ucd/server/conf/server/installed.properties
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"

echo "Starting the server now"
/opt/ibm-ucd/server/bin/server run
