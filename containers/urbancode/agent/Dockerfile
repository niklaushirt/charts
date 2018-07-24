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
ENV SERVER_ADDR="localhost"
ENV SERVER_PORT_JMS="7918"
ENV SERVER_PORT_HTTP="7919"
# Note: Latest version of kubectl may be found at:
# https://aur.archlinux.org/packages/kubectl-bin/
ENV KUBE_LATEST_VERSION="v1.10.2"
# Note: Latest version of helm may be found at:
# https://github.com/kubernetes/helm/releases
ENV HELM_VERSION="v2.9.1"
ENV HOME=/config

# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------
# Install kubectl and helm command line
# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------
RUN apk add --no-cache ca-certificates bash git \
    && wget -q https://storage.googleapis.com/kubernetes-release/release/${KUBE_LATEST_VERSION}/bin/linux/amd64/kubectl -O /usr/local/bin/kubectl \
    && chmod +x /usr/local/bin/kubectl \
    && wget -q http://storage.googleapis.com/kubernetes-helm/helm-${HELM_VERSION}-linux-amd64.tar.gz -O - | tar -xzO linux-amd64/helm > /usr/local/bin/helm \
    && chmod +x /usr/local/bin/helm


WORKDIR /config

RUN set -x && \
    # Create non-root user (with a randomly chosen UID/GUI).
    adduser kubectl -Du 2342 -h /config && \
    # Basic check it works.
    kubectl version --client



# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------
# Install DOCKER IN DOCKER
# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------

    RUN apk add --no-cache \
    		ca-certificates

    # set up nsswitch.conf for Go's "netgo" implementation (which Docker explicitly uses)
    # - https://github.com/docker/docker-ce/blob/v17.09.0-ce/components/engine/hack/make.sh#L149
    # - https://github.com/golang/go/blob/go1.9.1/src/net/conf.go#L194-L275
    # - docker run --rm debian:stretch grep '^hosts:' /etc/nsswitch.conf
    #RUN [ ! -e /etc/nsswitch.conf ] && echo 'hosts: files dns' > /etc/nsswitch.conf

    ENV DOCKER_CHANNEL stable
    ENV DOCKER_VERSION 18.06.0-ce
    # TODO ENV DOCKER_SHA256
    # https://github.com/docker/docker-ce/blob/5b073ee2cf564edee5adca05eee574142f7627bb/components/packaging/static/hash_files !!
    # (no SHA file artifacts on download.docker.com yet as of 2017-06-07 though)

    RUN set -ex; \
    # why we use "curl" instead of "wget":
    # + wget -O docker.tgz https://download.docker.com/linux/static/stable/x86_64/docker-17.03.1-ce.tgz
    # Connecting to download.docker.com (54.230.87.253:443)
    # wget: error getting response: Connection reset by peer
    	apk add --no-cache --virtual .fetch-deps \
    		curl \
    		tar \
    	; \
    	\
    # this "case" statement is generated via "update.sh"
    	apkArch="$(apk --print-arch)"; \
    	case "$apkArch" in \
    		x86_64) dockerArch='x86_64' ;; \
    		armhf) dockerArch='armel' ;; \
    		aarch64) dockerArch='aarch64' ;; \
    		ppc64le) dockerArch='ppc64le' ;; \
    		s390x) dockerArch='s390x' ;; \
    		*) echo >&2 "error: unsupported architecture ($apkArch)"; exit 1 ;;\
    	esac; \
    	\
    	if ! curl -fL -o docker.tgz "https://download.docker.com/linux/static/${DOCKER_CHANNEL}/${dockerArch}/docker-${DOCKER_VERSION}.tgz"; then \
    		echo >&2 "error: failed to download 'docker-${DOCKER_VERSION}' from '${DOCKER_CHANNEL}' for '${dockerArch}'"; \
    		exit 1; \
    	fi; \
    	\
    	tar --extract \
    		--file docker.tgz \
    		--strip-components 1 \
    		--directory /usr/local/bin/ \
    	; \
    	rm docker.tgz; \
    	\
    	apk del .fetch-deps; \
    	\
    	dockerd -v; \
    	docker -v

    COPY ./common/modprobe.sh /usr/local/bin/modprobe
    COPY ./common/docker-entrypoint.sh /usr/local/bin/


# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------
# Install URBANCODE AGENT
# ---------------------------------------------------------------------------------------------
# ---------------------------------------------------------------------------------------------

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
