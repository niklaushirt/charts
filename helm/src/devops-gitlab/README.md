# GitLab Helm Chart



## Chart Details

This chart will do the following:

* 1 x GitLab Mexposed on an external LoadBalancer
* All using Kubernetes Deployments

## Installing the Chart

To install the chart with the release name `my-release`:
```sh
$ helm init
$ helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/
$ helm fetch mycharts/gitlab-ce
```

```bash
$ helm install --name gitlab mycharts/gitlab-ce

```
