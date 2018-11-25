#!/bin/bash
. ./experiment.sh # Import functions to manage replicas / clients :)

function usage {
	echo "Usage:"
	echo "run [num_replicas] [properties_file] [num_clients]"
	echo "stop [num_replicas] [num_clients]"
	echo "clear_logs"
	echo "clear_jars"
}

if [[ $# -lt 1 ]]; then
    echo "Not enough arguments"
    usage
    exit 1
fi

case "$1" in
	"help")
		usage
		;;
	"run")
		clear_logs
		start_replica_managers $2 $3 $5 # $2=#reps, $3=.properties file
		sleep 10s
		start_replicas $2
		sleep 10s
		start_clients $4 $3 $5 # $4=#clients $5=puppetmaster address
		echo "Experiment started, do not forget to stop it!"
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

