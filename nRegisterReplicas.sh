#!/bin/bash

if [[ $# -lt 2 ]]; then
    echo "Not enough arguments"
    exit 1
fi

if [ -d jpaxosLogs ]; then
	echo "Found log folder, deleting"
	rm -rf jpaxosLogs
fi

if [ ! -f ./target/scala-2.12/ReplicaManager.jar ]; then
	echo "ReplicaManager.jar does not exist, creating it"
	sbt assembly
fi

function clearLog {
	if [ -f $1 ]; then
		echo "$1 exists, deleting it"
		rm $1
	fi
}

for i in `seq 0 $(($1 - 1))`; do
	log="replica$i.out"
	clearLog $log
	comm="./loop_replica.sh $i $2 > $log &"
	eval $comm
done

tail -f *.out

