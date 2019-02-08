#!/bin/bash
#-----------------------------------------------------------------------------------------------------------------
#REMOVE PACKAGES
#-----------------------------------------------------------------------------------------------------------------
rm ./*.tgz

#REMOVE INDEX.YAML
rm ./index.yaml

#-----------------------------------------------------------------------------------------------------------------
#CREATE PACKAGES
#-----------------------------------------------------------------------------------------------------------------
helm package datadog
helm package demoliberty
helm package devops-gitlab
helm package devops-jenkins
helm package devops-ucd-server
helm package devops-ucd-agent
helm package kubernetes-dashboard
helm package mariadb
helm package mongodb
helm package nginx
helm package nodered
helm package openwhisk
helm package sonarqube
helm package tomcat
helm package velocity
helm package ibm-blockchain-network
helm package gbapp
helm package manageiq

#HACK for DEMO LIBERTY
cp ../../containers/demoliberty/chart_versions/*.tgz .

#CREATE INDEX.YAML
helm repo index --url https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/ ./

#-----------------------------------------------------------------------------------------------------------------
#REMOVE PACKAGES in stable
#-----------------------------------------------------------------------------------------------------------------
rm ../charts/repo/stable/*.tgz


#REMOVE INDEX.YAML in charts
rm ../charts/repo/stable/index.yaml


#-----------------------------------------------------------------------------------------------------------------
#COPY PACKAGES
#-----------------------------------------------------------------------------------------------------------------
cp  ./*.tgz ../charts/repo/stable/



#COPY INDEX.YAML
cp index.yaml ../charts/repo/stable/index.yaml
