#!/bin/bash


echo "santi p"
scp target/tpepod-49244-0.1.one-jar.jar gmarucci@santip:/Users/gmarucci/manu

echo "guido"
scp target/tpepod-49244-0.1.one-jar.jar gmarucci@guido:/Users/gmarucci/manu
echo "guido2"
scp target/tpepod-49244-0.1.jar gmarucci@guido:/Users/gmarucci/manu/target

echo "manu"
scp target/tpepod-49244-0.1.one-jar.jar gmarucci@manu:/Users/gmarucci/manu

