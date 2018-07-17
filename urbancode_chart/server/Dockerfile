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

#Copy the files to tmp folder
COPY ./common /tmp/

# install the UCD Server and remove the install files.
RUN unzip -q /tmp/*.zip -d /tmp
RUN cat /tmp/install.properties >> /tmp/ibm-ucd-install/install.properties
RUN 	sh /tmp/ibm-ucd-install/install-server.sh
RUN 	rm -rf /tmp/ibm-ucd-install
RUN 	rm -f /tmp/install.properties


# HTTP, HTTPS, JMS
EXPOSE 8080
EXPOSE 8443
EXPOSE 7918
EXPOSE 7919



# Set work directory for/and command
WORKDIR /tmp
CMD ["sh", "startServer.sh"]
