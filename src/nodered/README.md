![Node Red logo](https://nodered.org/about/resources/media/node-red-icon-2.png)

# Node-RED

http://nodered.org


A visual tool for wiring the Internet of Things.

![Node-RED: A visual tool for wiring the Internet of Things](http://nodered.org/images/node-red-screenshot.png)

## Getting Help

More documentation can be found [here](http://nodered.org/docs).

For further help, or general discussion, please use the
[mailing list](https://groups.google.com/forum/#!forum/node-red).


# Introduction

This chart deploys a single NodeRed instance into an IBM Cloud private or other Kubernetes environment.

## Prerequisites

- Kubernetes 1.7 or greater, with beta APIs enabled
- You need a PersistentVolumeof with access mode "ReadWriteMany".

## Installing the Chart

To install the chart with the release name `nodered`:

```sh
helm install --name nodered DEMO/nodered
```

> **Tip**: See all the resources deployed by the chart using `kubectl get all -l release=nodered`

## Uninstalling the Chart

To uninstall/delete the `nodered` release:

```sh
helm delete nodered
```

The command removes all the Kubernetes components associated with the chart, except any Persistent Volume Claims (PVCs).  This is the default behavior of Kubernetes, and ensures that valuable data is not deleted.  In order to delete the PVC use the following command:

```sh
kubectl delete pvc -l release=nodered
```
# Version  for Node Red
tag: 0.17.5

#Name and Size for PV
persistentVolumeClaim: "nodered-pv-claim"
persistentVolumeClaimSize: "2Gi"

#Resource limits
memoryLimit: "512Mi"
cpuLimit: 0.6

#Node Port
nodePort: 30750


## Configuration
The following table lists the configurable parameters of the `ibm-mqadvanced-server-dev` chart and their default values.

| Parameter                        | Description                                     | Default                                                    |
| -------------------------------- | ----------------------------------------------- | ---------------------------------------------------------- |
| `tag`                        | Version for Node Red Image  | `0.17.5`                                     |
| `persistentVolumeClaim`                        | PersistentVolumeClaim Name  | `nodered-pv-claim`                                     |
| `persistentVolumeClaimSize`                        | PersistentVolumeClaim Size  | `2Gi`                                     |
| `memoryLimit`                        | Memory Limit  | `512Mi`                                     |
| `cpuLimit`                        | CPU Limit  | `0.6`                                     |
| `nodePort`                        | Node Port for access  | `30750`                                     |

Specify each parameter using the `--set key=value[,key=value]` argument to `helm install`.

Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart.

> **Tip**: You can use the default [values.yaml](values.yaml)

## Persistence

The chart mounts a [Persistent Volume](http://kubernetes.io/docs/user-guide/persistent-volumes/).

# Copyright

© Copyright Niklaus Hirt 2017
