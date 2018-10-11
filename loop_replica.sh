#!/bin/bash

comm="java -jar Replica/target/scala-2.12/ReplicaManager.jar $1 $2 $((7000 + $1)) http://127.0.0.1:9090"
while true
do
    eval ${comm}
    if [ $? -eq 0 ]; then
        break
    fi
    echo "Replica $1 was killed. Respawning.."
done

