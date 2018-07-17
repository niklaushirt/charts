{{/* vim: set filetype=mustache: */}}
{{/*
Consumer name
*/}}
{{- define "velocity.consumer.name" -}}
  {{- $prefix := default "velocity" .Values.prefixname | trunc 20 | trimSuffix "-" -}}
  {{- printf "%s-%s" $prefix "consumer" -}}
{{- end -}}

{{/*
UI name
*/}}
{{- define "velocity.ui.name" -}}
  {{- $prefix := default "velocity" .Values.prefixname | trunc 20 | trimSuffix "-" -}}
  {{- printf "%s-%s" $prefix "ui" -}}
{{- end -}}

{{/*
Ingress name
*/}}
{{- define "velocity.ingress.name" -}}
  {{- $prefix := default "velocity" .Values.prefixname | trunc 20 | trimSuffix "-" -}}
  {{- printf "%s-%s" $prefix "ingress" -}}
{{- end -}}

{{/*
Consumer URL
*/}}
{{- define "velocity.consumer.url" -}}
  {{- if .Values.ingress -}}
    {{- printf "%s://%s/%s" .Values.url.protocol .Values.url.domain "velocity-consumer" -}}
  {{- else if eq .Values.consumer.service.type "NodePort" -}}
    {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.consumer.service.nodePort -}}
  {{- else -}}
    {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.consumer.service.externalPort -}}
  {{- end -}}
{{- end -}}

{{/*
UI URL
*/}}
{{- define "velocity.ui.url" -}}
  {{- if eq .Values.url.protocol "https" -}}
    {{- if .Values.ingress -}}
      {{- printf "%s://%s/%s" .Values.url.protocol .Values.url.domain "velocity" -}}
    {{- else if eq .Values.ui.service.type "NodePort" -}}
      {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.ui.service.nodeSSLPort -}}
    {{- else -}}
      {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.ui.service.externalSSLPort -}}
    {{- end -}}
  {{- else -}}
    {{- if .Values.ingress -}}
      {{- printf "%s://%s/%s" .Values.url.protocol .Values.url.domain "velocity" -}}
    {{- else if eq .Values.ui.service.type "NodePort" -}}
      {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.ui.service.nodePort -}}
    {{- else -}}
      {{- printf "%s://%s:%g" .Values.url.protocol .Values.url.domain .Values.ui.service.externalPort -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
