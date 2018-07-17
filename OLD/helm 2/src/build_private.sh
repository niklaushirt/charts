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
helm package ghost
helm package jenkins
helm package kubernetes-dashboard
helm package mariadb
helm package microclimate
helm package mongodb
helm package mqadvanceddev
helm package nginx
helm package nodered
helm package redis
helm package sonarqube
helm package tomcat
helm package wordpress
helm package velocity

helm package datadog
helm package gitlab-ce
helm package gitlab
helm package kube-ops-view
helm package weave-scope
helm package openwhisk
helm package ucd-server
helm package ucd-agent
helm package demoliberty

#CREATE INDEX.YAML
helm repo index --url https://raw.githubusercontent.com/niklaushirt/charts/master/charts/repo/stable/ ./

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