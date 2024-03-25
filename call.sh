#!/bin/bash

while true; do
  str=`~/oc/4.11/oc project $1`
  echo Output: $str
  # Use the below when you want the output not to contain some string
  if [[ $str =~ intersmash-prod ]]; then
    break
  fi
  sleep .5
done

~/oc/4.11/oc get pods
