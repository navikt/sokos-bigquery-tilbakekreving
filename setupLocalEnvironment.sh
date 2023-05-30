#!/bin/bash

# Ensure user is authenicated, and run login if not.
gcloud auth print-identity-token &> /dev/null
if [ $? -gt 0 ]; then
    gcloud auth login
fi
kubectl config use-context dev-fss
kubectl config set-context --current --namespace=okonomi

# Get bigquery system variables
envValue=$(kubectl exec -it $(kubectl get pods | grep sokos-bigquery-tilbakekreving | cut -f1 -d' ') -c sokos-bigquery-tilbakekreving  -- env | egrep -A11 "GOOGLE_APPLICATION_CREDENTIALS"  )

# Set bigquery as local environment variables
rm -f defaults.properties

echo "$envValue" |   tr '[\n\r]' ' '> defaults.properties
echo "GOOGLE_APPLICATION_CREDENTIALS stores as defaults.properties"
