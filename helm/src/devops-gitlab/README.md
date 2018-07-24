# GitLab Helm Chart

> **This Chart is part of the Demo DevOps Pipeline**


GitLab is the first single application built from the ground up for all stages of the DevOps lifecycle for Product, Development, QA, Security, and Operations teams to work concurrently on the same project. GitLab enables teams to collaborate and work from a single conversation, instead of managing multiple threads across disparate tools. GitLab provides teams a single data store, one user interface, and one permission model across the DevOps lifecycle allowing teams to collaborate, significantly reducing cycle time and focus exclusively on building great software quickly.

![GL_MAP](https://github.com/niklaushirt/charts/raw/master/helm/charts/icons/gitlab_bp.png)



## Pre-requisite

- Kubernetes 1.4+ with Beta APIs enabled
- Helm v2.6  (version might vary, which ever is compatible with your Kuberetes cluster).

## Installing the Chart and images

1. Get the required helm charts.

  ```sh
  $ helm init
  $ helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/
  $ helm fetch mycharts/devops-gitlab
  ```

2. Refer to the Configuration section below to customize the deployment. The following step (step 3) shows the values at minimal that are required to be set.

3. Deploy to your Kubernetes cluster.

  ```sh
  $ helm install --name gitlab mycharts/devops-gitlab

  ```

4. Read the NOTES section from the result of the previous command.

## Configuration

The following tables lists the configurable parameters of the GitLab chart and their default values.

Parameter                     | Description                                                                                        |  Default
----------------------------- | ---------------------------------------------------------------------------------------------------| ---------------------
image.name | Image for GitLab | gitlab/gitlab-ce    
image.tag | Version of GitLab | 9.3.8-ce.0                                                                                                             
pullPolicy                    | Pull Policy | IfNotPresent
service.name                   | Service Name | gitlab-ce
service.type                    | Service Type | NodePort
service.http                    | NodePort for HTTP communication | 30126
service.https                    | NodePort for HTTPS communication | 30127

## Uninstalling the release

NOTE: this will remove all GitLab pod deployments, services and ingress rules (if enabled) that were installed on your Kube cluster as part of this release.

```sh
$ helm delete gitlab --purge
```
