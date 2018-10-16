#!/bin/bash

if [[ $# -lt 1 ]]; then
    echo "Not enough arguments"
    exit 1
fi

log_folder="logs"

if [ ! -d $log_folder ]; then
	echo "Create directory to store logs"
	mkdir $log_folder
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
		log="$log_folder/replica$i.out"
		clearLog $log
		# Listens on 7000 + i
		comm="./loop_replica.sh $i $2 > $log &"
		eval $comm
	done
	echo "Starting replica managers"
}

function start_clients {
	if [ ! -f ./Client/target/scala-2.12/Spammer.jar ]; then
		echo "Spammer.jar does not exist, creating it"
		sbt "Client / assembly"
	fi

	for i in `seq 0 $(($1 - 1))`; do
		log="$log_folder/client$i.out"
		clearLog $log
		# Listens on 8000 + i, contacts Master @ 127.0.0.1:9090
		comm="java -jar Client/target/scala-2.12/Spammer.jar $2 $((8000 + $i)) > $log &"
		echo $comm
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
	rm -rf jpaxosLogs
	rm -rf $log_folder/*.out
}

case "$1" in
	"help")
		usage
		;;
	"run")
		clear_logs
		start_replica_managers $2 $3 # $2=#reps, $3=.properties file
		sleep $4
		start_replicas $2
		sleep $4
		start_clients $4 $3 # $4=#clients
		tail -f $log_folder/*.out
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
	"start_log_server")
		sbt "runMain ch.qos.logback.classic.net.SimpleSocketServer 6000 server_logback.xml"
		;;
esac

