#!/bin/bash

java -Djava.security.policy=file.policy -Djava.rmi.serv.codebase="http://labu166:8888/tpepod-49244-0.1.jar" -jar target/tpepod-49244-0.1.one-jar.jar $@