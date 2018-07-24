export AGENT_NAME="agent"
#export SERVER_ADDR="9.30.189.183"
export SERVER_ADDR="9.30.250.6"
export SERVER_PORT_UI="30123"
export SERVER_PORT_JMS="30124"
export SERVER_PORT_HTTP="30125"
export UCDA_PATH=/opt/ibm-ucd/agent
export ARCHITECTURE=linux #mac or linux

#-----------------------------------------------------------------------------
#-----------------------------------------------------------------------------
# DO NOT MODIFY BELOW
#-----------------------------------------------------------------------------
#-----------------------------------------------------------------------------

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/jre/"
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

#sudo apt-get install openjdk-8-jdk
#update-alternatives --config java


#Mon Jul 23 06:58:29 PDT 2018
# IBM\ UrbanCode\ Deploy/java.home=/usr/lib/jvm/java-8-openjdk-amd64/jre
# agent.HttpFailoverHandler.disabled=null
# agentcomm.enabled=true
# agentcomm.server.uri=random\:(wss\://9.30.250.6\:30125)
# encryption.keystore=../conf/encryption.keystore
# encryption.keystore.alias=aes128keybaxd
# encryption.keystore.password=pbe{xlT8V1t3CEmoVVlvilBHemTEByBVh4ta94J8JNQJJ44\=}
# locked/agent.brokerUrl=failover\:()
# locked/agent.home=/opt/ibm-ucd/agent
# locked/agent.id=wVRsjhXotpUzwUgYPnpA
# locked/agent.keystore=../conf/agent.keystore
# locked/agent.keystore.pwd=pbe{v1Q7ev9lcWszZIpMMlykVrnrJaEswgJZ3TnotMbG0bs\=}
# locked/agent.mutual_auth=null
# locked/agent.name=LOCAL
# system.default.encoding=UTF-8
# verify.server.identity=null

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


echo $SERVER_ADDR"  ucd-server" >> /etc/hosts


cat ./ucd-agent-install/agent.install.properties

sh ./ucd-agent-install/install-agent-from-file.sh agent.install.properties
#rm -rf ./ucd-agent-install
#rm -f ./agent.install.properties


echo "Starting the agent now"
$UCDA_PATH/bin/agent run
