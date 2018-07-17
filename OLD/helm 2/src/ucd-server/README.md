# UrbanCode Deploy Server

IBM UrbanCode Deploy is a tool for automating application deployments through your environments. It is designed to facilitate rapid feedback and continuous delivery in agile development while providing the audit trails, versioning and approvals needed in production.
This chart installs the UCD Server.


![UCD_MAP](https://developer.ibm.com/urbancode/wp-content/themes/projectnext-urbancode/images/products/deploy/deploy-infographic.png)



## Pre-requisite

- Kubernetes 1.4+ with Beta APIs enabled
- Helm v2.6  (version might vary, which ever is compatible with your Kuberetes cluster).

## Installing the Chart and images

1. Get the required helm charts.

  ```sh
  $ helm init
  $ helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/charts/repo/stable/
  $ helm fetch mycharts/ucd-server
  ```

2. Refer to the Configuration section below to customize the deployment. The following step (step 3) shows the values at minimal that are required to be set.

3. Deploy to your Kubernetes cluster.

  ```sh
  $ helm install --name ucds mycharts/ucd-server
  ```

4. Read the NOTES section from the result of the previous command.

## Configuration

The following tables lists the configurable parameters of the UCD Server chart and their default values.

Parameter                     | Description                                                                                        |  Default
----------------------------- | ---------------------------------------------------------------------------------------------------| ---------------------
tag | Version of the UCD | 7.0.0                                                                                                            
serviceType                    | Type of the Kubernetes Service | Load Balancer


## Uninstalling the release

NOTE: this will remove all UCD Server pod deployments, services and ingress rules (if enabled) that were installed on your Kube cluster as part of this release.

```sh
$ helm delete ucds --purge
```
