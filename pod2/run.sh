#!/bin/bash

java -Djava.security.policy=file.policy -Djava.rmi.server.codebase="http://192.168.3.4:8888/target/tpepod-49244-0.1.jar" -jar target/tpepod-49244-0.1.one-jar.jar $@