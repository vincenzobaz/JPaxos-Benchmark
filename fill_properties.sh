#!/bin/bash
if [[ $# -lt 2 ]]; then
    echo "Not enough arguments"
    exit 1
fi

generated="generated-paxos.properties"

if [ -f $generated ]; then
	echo "Found $generated, deleting"
	rm $generated
fi

function prepare_settings {
	n=$1
	template=$2

	replicas=""

	for i in `seq 0 $(($n - 1))`; do
		replicas_port=$((2000 + $i))
		clients_port=$((3000 + $i))
		host="process.$i = localhost:$replicas_port:$clients_port"
		replicas="$replicas$host\n"
	done

	printf "$replicas\n" > $generated
	cat $template >> $generated
	dos2unix $generated
	echo "Generated $generated"
}

prepare_settings $1 $2

