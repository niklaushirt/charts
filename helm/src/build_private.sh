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
helm package gitlab-ce
helm package jenkins
helm package kubernetes-dashboard
helm package mariadb
helm package mongodb
helm package nginx
helm package nodered
helm package openwhisk
helm package sonarqube
helm package tomcat
helm package ucd-server
helm package ucd-agent
helm package velocity



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

#HACK for DEMO LIBERTY
cp ../../demoliberty/chart_versions/*.tgz ../charts/repo/stable/