#!/bin/bash
#-----------------------------------------------------------------------------------------------------------------
#REMOVE PACKAGES
#-----------------------------------------------------------------------------------------------------------------
#rm ./libertysimple-1.0.0.tgz

#REMOVE INDEX.YAML
rm ./index.yaml

#-----------------------------------------------------------------------------------------------------------------
#CREATE PACKAGES
#-----------------------------------------------------------------------------------------------------------------
helm package ups-broker
helm package catalog

#CREATE INDEX.YAML
helm repo index --url https://raw.githubusercontent.com/niklaushirt/servicebroker/master/charts/repo/stable/ ./

#-----------------------------------------------------------------------------------------------------------------
#REMOVE PACKAGES in charts
#-----------------------------------------------------------------------------------------------------------------
#rm ../stable/repo/stable/libertysimple-1.0.0.tgz

#REMOVE INDEX.YAML in charts
rm ../charts/repo/stable/index.yaml


#-----------------------------------------------------------------------------------------------------------------
#COPY PACKAGES
#-----------------------------------------------------------------------------------------------------------------

cp ups-broker-0.0.1.tgz ../charts/repo/stable/
cp catalog-0.0.1.tgz ../charts/repo/stable/

#COPY INDEX.YAML
cp index.yaml ../charts/repo/stable/index.yaml
