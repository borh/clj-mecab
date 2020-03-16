#!/usr/bin/env bash

G=$(ls src)
A=$(basename $PWD)
V=$(git tag|tail -1|tr -d v) 

echo Setting $G $A $V in meyvn.edn...

sed -re "s/:group-id    \"(.+)\"/:group-id    \"$G\"/" -i meyvn.edn
sed -re "s/:artifact-id \"(.+)\"/:artifact-id \"$A\"/" -i meyvn.edn
sed -re "s/:version     \"(.+)\"/:version     \"$V\"/" -i meyvn.edn
sed -re "s|\"https://clojars.org/(.+)\"|\"https://clojars.org/$G/$A\"|" -i meyvn.edn
