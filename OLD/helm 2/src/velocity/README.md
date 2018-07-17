# UrbanCode Velocity

Velocity brings reporting and analytics dashboard for UrbanCode Deploy.

## Pre-requisite

- Kubernetes 1.4+ with Beta APIs enabled
- Helm v2.6  (version might vary, which ever is compatible with your Kuberetes cluster).
- MongoDB with persistent volume. <https://github.com/kubernetes/charts/tree/master/stable/mongodb>
- Your UrbanCode Velocity access key. Get one for FREE from the UrbanCode Velocity access site. <br/> http://di-kube.us-south.containers.mybluemix.net:32018/

## Installing the Chart and images

1. Get the required helm charts.

  ```sh
  $ helm init
  $ helm repo add urbancode https://raw.githubusercontent.com/IBM/velocity/master/kubernetes/repo
  $ helm fetch urbancode/velocity
  ```

2. Refer to the Configuration section below to customize the deployment. The following step (step 3) shows the values at minimal that are required to be set.

3. Deploy to your Kubernetes cluster.

  ```sh
  $ helm install \
    --set access.key=<velocity-access-key> \
    --set url.domain=<sub.domain.com> \
    --set mongo.url=mongodb://<mongo-username>:<mongo-password>@<mongo-service-name/URL>:27017/<database-name> \
    --set encrypt.key=<a-unique-guid/string-for-data-encryption> \
    --set adminpassword=admin \
    --name my-release urbancode/velocity
  ```

4. Read the NOTES section from the result of the previous command.

## Configuration

The following tables lists the configurable parameters of the Velocity chart and their default values.

Parameter                     | Description
----------------------------- | ---------------------------------------------------------------------------------------------------
access.key                   | Your Velocity access key. Get one for FREE from the UrbanCode Velocity registration site. <br/> http://di-kube.us-south.containers.mybluemix.net:32018/
prefixname                    | `velocity` A prefix to be added to all pods, services and ingress names. Limit to 20 characters.
url.protocol                  | `https` http or https
url.domain                    | This is usually the the hostname of your Kubernetes master node or ingress hostname. <br/>  If you have any reverse proxy in front of your Kubernetes cluster, that becomes your domain. <br/>
mongo.url                     | `mongodb://<mongo-username>:<mongo-password>@<mongo-service-name/URL>:<mongo-port>/<database-name>`
encrypt.key                   | A unique id used to encrypt user names, tokens and any email addresses in mongoDB.
adminpassword                 | `admin`
consumer.image.repository     | `velocity-conusmer` Consumer image name.
consumer.image.tag            | `latest` Consumer image tag.
consumer.image.pullPolicy     | `Always`
consumer.service.type         | service type `NodePort`
consumer.service.nodePort     | Maps internalPort to NodePort
consumer.service.externalPort | Maps internalPort to externalPort
ui.image.repository           | `velocity-ui` UI image name.
ui.image.tag                  | `latest` UI image tag.
ui.image.pullPolicy           | `Always`
ui.service.type               | service type `NodePort`
ui.service.nodePort           | Maps internal Port to NodePort
ui.service.nodeSSLPort        | Maps internal SSL Port to NodePort
ui.service.externalPort       | Maps internalPort to externalPort
ui.service.externalSSLPort    | Maps internal SSL Port to external SSL Port
ingress.enabled               | `false` to enable ingress.
loglevel                      | `error`  Logging level. Possible values, all < debug < info < warn < error < fatal < off

## Uninstalling the release

NOTE: this will remove all Velocity pod deployments, services and ingress rules (if enabled) that were installed on your Kube cluster as part of this release.

```sh
$ helm delete my-release
```
