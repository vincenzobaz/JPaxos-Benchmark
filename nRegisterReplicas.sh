#!/bin/bash

if [[ $# -lt 3 ]]; then
    echo "Not enough arguments"
    exit 1
fi

# Clear logs
if [ -d jpaxosLogs ]; then
	echo "Found log folder, deleting"
	rm -rf jpaxosLogs
fi

# Check if jars exist
if [ ! -f ./Replica/target/scala-2.12/ReplicaManager.jar ]; then
	echo "ReplicaManager.jar does not exist, creating it"
	sbt "Replica / assembly"
fi
if [ ! -f ./Client/target/scala-2.12/Spammer.jar ]; then
	echo "ReplicaManager.jar does not exist, creating it"
	sbt "Client / assembly"
fi

function clearLog {
	if [ -f $1 ]; then
		echo "$1 exists, deleting it"
		rm $1
	fi
}

# Launch the replicas
for i in `seq 0 $(($1 - 1))`; do
	log="replica$i.out"
	clearLog $log
	# Listens on 7000 + i
	comm="./loop_replica.sh $i $2 > $log &"
	eval $comm
done

# Launch the Clients
for i in `seq 0 $(($3 - 1))`; do
	log="client$i.out"
	clearLog $log
	# Listens on 8000 + i, contacts Master @ 127.0.0.1:9090
	comm="java -jar Client/target/scala-2.12/Spammer.jar paxos.properties $((8000 + i)) > $log &"
	eval $comm
done

tail -f *.out
