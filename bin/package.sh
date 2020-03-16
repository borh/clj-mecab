#!/usr/bin/env bash
myvn package -g
mv meyvn-pom.xml pom.xml
clj -Spom
mvn -f pom.xml package
