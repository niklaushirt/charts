#!/bin/bash
#-----------------------------------------------------------------------------------------------------------------
#REMOVE PACKAGES
#-----------------------------------------------------------------------------------------------------------------

rm ./ghost-0.4.15.tgz
rm ./jenkins-2.19.4.tgz
rm ./kubernetes-dashboard-0.4.2.tgz
rm ./mariadb-2.1.1.tgz
rm ./microclimate-0.1.0-beta.tgz
rm ./mongodb-0.4.18.tgz
rm ./mqadvanceddev-1.0.2.tgz
rm ./nginx-1.8.1.tgz
rm ./nodered-0.17.5.tgz
rm ./redis-3.2.0.tgz
rm ./sonarqube-0.2.0.tgz
rm ./tomcat-9.0.tgz
rm ./urbancode-6.2.7.tgz
rm ./wordpress-0.6.10.tgz
#rm ./xxxxx.tgz



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
helm package urbancode
helm package wordpress


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



cp  ./ghost-0.4.15.tgz ../charts/repo/stable/
cp  ./jenkins-2.19.4.tgz ../charts/repo/stable/
cp  ./kubernetes-dashboard-0.4.2.tgz ../charts/repo/stable/
cp  ./mariadb-2.1.1.tgz ../charts/repo/stable/
cp  ./microclimate-0.1.0-beta.tgz ../charts/repo/stable/
cp  ./mongodb-0.4.18.tgz ../charts/repo/stable/
cp  ./mqadvanceddev-1.0.2.tgz ../charts/repo/stable/
cp  ./nginx-1.8.1.tgz ../charts/repo/stable/
cp  ./nodered-0.17.5.tgz ../charts/repo/stable/
cp  ./redis-3.2.0.tgz ../charts/repo/stable/
cp  ./sonarqube-0.2.0.tgz ../charts/repo/stable/
cp  ./tomcat-9.0.tgz ../charts/repo/stable/
cp  ./urbancode-6.2.7.tgz ../charts/repo/stable/
cp  ./wordpress-0.6.10.tgz ../charts/repo/stable/


#COPY INDEX.YAML
cp index.yaml ../charts/repo/stable/index.yaml
