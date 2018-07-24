# UrbanCode Deploy Server

> **This Chart is part of the Demo DevOps Pipeline**

IBM UrbanCode Deploy is a tool for automating application deployments through your environments. It is designed to facilitate rapid feedback and continuous delivery in agile development while providing the audit trails, versioning and approvals needed in production.
This chart installs the UCD Server.

![UCD_MAP](https://github.com/niklaushirt/charts/raw/master/helm/charts/icons/ucd_bp.png)



## Pre-requisite

- Kubernetes 1.4+ with Beta APIs enabled
- Helm v2.6  (version might vary, which ever is compatible with your Kuberetes cluster).

## Installing the Chart and images

1. Get the required helm charts.

  ```sh
  $ helm init
  $ helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/
  $ helm fetch mycharts/devops-ucd-server
  ```

2. Refer to the Configuration section below to customize the deployment. The following step (step 3) shows the values at minimal that are required to be set.

3. Deploy to your Kubernetes cluster.

  ```sh
  $ helm install \
      --set server.serviceType=LoadBalancer \
      --set server.nodeport.ui=30123 \
      --set server.nodeport.jms=30124 \
      --set server.nodeport.http=30125 \
      --name ucds mycharts/devops-ucd-server

  ```

4. Read the NOTES section from the result of the previous command.

## Configuration

The following tables lists the configurable parameters of the UCD Server chart and their default values.

Parameter                     | Description                                                                                        |  Default
----------------------------- | ---------------------------------------------------------------------------------------------------| ---------------------
image.tag | Version of the UCD | 7.0.0                                                                                                            
server.serviceType                    | Type of the Kubernetes Service | Load Balancer
server.nodeport.ui                    | NodePort to reach UI | 7918
server.nodeport.jms                    | NodePort for JMS communication | 7918
server.nodeport.http                    | NodePort for HTTP communication | 7919

## Uninstalling the release

NOTE: this will remove all UCD Server pod deployments, services and ingress rules (if enabled) that were installed on your Kube cluster as part of this release.

```sh
$ helm delete ucds --purge
```
