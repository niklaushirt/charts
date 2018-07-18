# A Chart for UrbanCode Deploy

## Build locally

```bash
git clone https://github.com/niklaushirt/charts.git
cd charts/containers/urbancode
docker build -t "ucds:7.0.0" ./server
docker build -t "ucda:7.0.0" ./agent
```

## Run locally

```bash
docker network create --subnet=172.99.0.0/16 ucd_net

docker run -d --rm --name ucd-server --ip 172.99.0.2 --network ucd_net -p 8443:8443 -p 7918:7918 -p 8080:8080 -t ucds:7.0.0

docker run -d --rm --name ucd-agent --network ucd_net -e "AGENT_NAME=myagent" -e "SERVER_ADDR=172.99.0.2" -e "SERVER_PORT_JMS=7918" -e "SERVER_PORT_HTTP=7918" -t ucda:7.0.0
docker run -d --rm --name ucd-agent --network ucd_net -e "AGENT_NAME=myagent1" -e "SERVER_ADDR=172.99.0.2" -e "SERVER_PORT_JMS=7918" -e "SERVER_PORT_HTTP=7918" -t ucda:7.0.0
docker run -d --rm --name ucd-agent --network ucd_net -e "AGENT_NAME=myagent2" -e "SERVER_ADDR=172.99.0.2" -e "SERVER_PORT_JMS=7918" -e "SERVER_PORT_HTTP=7918" -t ucda:7.0.0
docker run -d --rm --name ucd-agent --network ucd_net -e "AGENT_NAME=myagent3" -e "SERVER_ADDR=172.99.0.2" -e "SERVER_PORT_JMS=7918" -e "SERVER_PORT_HTTP=7918" -t ucda:7.0.0
```



## Add HELM repository

```
https://raw.githubusercontent.com/niklaushirt/urbancode_chart/master/helm/charts/repo/stable/
```

## Test AGENT

```bash
docker build -t "ucda:7.0.0" ./agent
docker kill local_agent
docker run -d --rm --name local_agent -v /root/SRC:/src -e "AGENT_NAME=local_agent" -e "SERVER_ADDR=9.30.189.183" -e "SERVER_PORT_JMS=30124" -e "SERVER_PORT_HTTP=30125" -t ucda:7.0.0

docker exec -it local_agent /bin/sh

docker kill local_agent

```



```bash
docker exec -it ucd-server /bin/bash
```