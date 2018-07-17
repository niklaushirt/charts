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
echo "  Agent will be configured as follows "
echo "-----------------------------------------------------------------------------"
echo "UCD Agent Name          : "$AGENT_NAME
echo "UCD Server Address      : "$SERVER_ADDR
echo "UCD Server JMS Port     : "$SERVER_PORT_JMS
echo "UCD Server HTTP Port    : "$SERVER_PORT_HTTP
echo "-----------------------------------------------------------------------------"
echo "-----------------------------------------------------------------------------"


#Removing the lines from the file
sed -i '/agent.id/d' /opt/ibm-ucd/agent/conf/agent/installed.properties
sed -i '/agent.name/d' /opt/ibm-ucd/agent/conf/agent/installed.properties
sed -i '/agent.jms.remote.port/d' /opt/ibm-ucd/agent/conf/agent/installed.properties
sed -i '/agentcomm.server.uri/d' /opt/ibm-ucd/agent/conf/agent/installed.properties


agentName="agent-"$HOSTNAME
agentId=""
if [ $AGENT_NAME ]; then
    agentName=$AGENT_NAME
    agentId=$AGENT_NAME
fi

#Setting agent.name to "agent-<hostname_of_container>"
#Setting the agent.id to "" so that id is auto generated during start
echo "locked/agent.name="$agentName >> /opt/ibm-ucd/agent/conf/agent/installed.properties
echo "locked/agent.id="$agentId >> /opt/ibm-ucd/agent/conf/agent/installed.properties

if [ $SERVER_ADDR ]; then
  echo "" >>/etc/hosts
  echo $SERVER_ADDR" ucd-server" >>/etc/hosts
  echo $SERVER_ADDR" ucd-server added to hosts"
fi

if [ $SERVER_PORT_JMS ]; then
  echo "locked/agent.jms.remote.port="$SERVER_PORT_JMS >> /opt/ibm-ucd/agent/conf/agent/installed.properties
fi
if [ $SERVER_PORT_HTTP ]; then
  echo "agentcomm.server.uri=wss://ucd-server:"$SERVER_PORT_HTTP >> /opt/ibm-ucd/agent/conf/agent/installed.properties
fi


echo "Starting the agent now"
/opt/ibm-ucd/agent/bin/agent run
