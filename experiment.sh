#!/bin/bash

if [[ $# -lt 1 ]]; then
    echo "Not enough arguments"
    exit 1
fi

function clearLog {
	if [ -f $1 ]; then
		echo "$1 exists, deleting it"
		rm $1
	fi
}

function start_replicas {
	for i in `seq 0 $(($1 - 1))`; do
		echo "Starting replica $i"
		address="http://127.0.0.1:$((7000 + $i))/start"
		res="$(curl -s -G $address)"
		echo "Replica replied: $res"
	done
}

function stop_replicas {
	for i in `seq 0 $(($1 - 1))`; do
		echo "Stopping replica $i"
		address="http://127.0.0.1:$((7000 + $i))/stop"
		res="$(curl -s -G $address)"
		echo "Replica replied: $res"
	done
}

function start_replica_managers {
	if [ ! -f ./Replica/target/scala-2.12/ReplicaManager.jar ]; then
		echo "ReplicaManager.jar does not exist, creating it"
		sbt "Replica / assembly"
	fi

	for i in `seq 0 $(($1 - 1))`; do
		log="replica$i.out"
		clearLog $log
		# Listens on 7000 + i
		comm="./loop_replica.sh $i $2 > $log &"
		eval $comm
	done
	sleep 5
	echo "Starting replicas"
	./start_replicas.sh $1
}

function start_clients {
	if [ ! -f ./Client/target/scala-2.12/Spammer.jar ]; then
		echo "ReplicaManager.jar does not exist, creating it"
		sbt "Client / assembly"
	fi

	for i in `seq 0 $(($1 - 1))`; do
		log="client$i.out"
		clearLog $log
		# Listens on 8000 + i, contacts Master @ 127.0.0.1:9090
		comm="java -jar Client/target/scala-2.12/Spammer.jar $2 $((8000 + $i)) > $log &"
		eval $comm
	done
}

function stop_clients {
	for i in `seq 0 $(($1 - 1))`; do
		echo "Stopping client $i"
		address="http://127.0.0.1:$((8000 + $i))/stop"
		res="$(curl -s -G $address)"
		echo "Client replied: $res"
	done
}

function usage {
	echo "Usage:"
	echo "run [num_replicas] [properties_file] [num_clients]"
	echo "stop [num_replicas] [num_clients]"
	echo "clear_logs"
	echo "clear_jars"
}

function clear_logs {
	rm -rf paxosLogs
	rm *.out
}

case "$1" in
	"help")
		usage
		;;
	"run")
		clear_logs
		start_replica_managers $2 $3 # $2=#reps, $3=.properties file
		start_replicas $2
		start_clients $4 $3 # $4=#clients
		tail -f *.out
		;;
	"stop")
		stop_clients $3 # $3=#clients
		stop_replicas $2 # $2=#replicas
		;;
	"clear_logs")
		clear_logs
		;;
	"clear_jars")
		rm Client/target/scala-2.12/*.jar
		rm Replica/target/scala-2.12/*.jar
	;;
esac

