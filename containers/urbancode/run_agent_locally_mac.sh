export AGENT_NAME="agent"
export SERVER_ADDR="9.30.189.183"
export SERVER_PORT_UI="30123"
export SERVER_PORT_JMS="30124"
export SERVER_PORT_HTTP="30125"
export UCDA_PATH=~/TEMP/ucd_agent
export ARCHITECTURE=mac #mac or linux

#-----------------------------------------------------------------------------
#-----------------------------------------------------------------------------
# DO NOT MODIFY BELOW
#-----------------------------------------------------------------------------
#-----------------------------------------------------------------------------

export JAVA_HOME=$(/usr/libexec/java_home)
export timestamp=$(date +%s)

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


mkdir -p $UCDA_PATH/install_ucd_agent

cp ./config/*.zip $UCDA_PATH/install_ucd_agent
cp ./config/*.properties $UCDA_PATH/install_ucd_agent


cd $UCDA_PATH/install_ucd_agent


# install the UCD Agent and remove the install files.
unzip -q ./ucd-agent.zip -d .


cat ./agent.install_$ARCHITECTURE.properties > ./ucd-agent-install/agent.install.properties

agentName=$AGENT_NAME"-"$HOSTNAME"-"$timestamp

echo "locked/agent.home="$UCDA_PATH >> ./ucd-agent-install/agent.install.properties
echo "locked/agent.name="$agentName >> ./ucd-agent-install/agent.install.properties
echo "locked/agent.id="$agentName >> ./ucd-agent-install/agent.install.properties
echo "locked/agent.jms.remote.port="$SERVER_PORT_JMS >> ./ucd-agent-install/agent.install.properties
echo "agentcomm.server.uri=wss://"$SERVER_ADDR":"$SERVER_PORT_HTTP >> ./ucd-agent-install/agent.install.properties


cat ./ucd-agent-install/agent.install.properties

sh ./ucd-agent-install/install-agent-from-file.sh agent.install.properties

rm -rf ./ucd-agent-install
rm -f ./*.properties


echo "Starting the agent now"
$UCDA_PATH/bin/agent run
