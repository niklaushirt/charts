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
#


FROM ibmjava:8-sfj-alpine

MAINTAINER Niklaus Hirt <niklaushirt@gmail.com>

ENV AGENT_NAME="agent"
ENV SERVER_ADDR="aaaa"
ENV SERVER_PORT_JMS="7918"
ENV SERVER_PORT_HTTP="7919"

#Copy the files to tmp folder
COPY ./common /tmp/

# install the UCD Agent and remove the install files.
RUN unzip -q /tmp/ucd-agent.zip -d /tmp
RUN   cat /tmp/agent.install.properties >> /tmp/ucd-agent-install/agent.install.properties
RUN 	sh /tmp/ucd-agent-install/install-agent-from-file.sh agent.install.properties
RUN 	rm -rf /tmp/ucd-agent-install
RUN 	rm -f /tmp/agent.install.properties


# JMS
EXPOSE 7918
#HTTP
EXPOSE 7919


# Set work directory for/and command
WORKDIR /tmp
CMD ["sh", "startAgent.sh"]
