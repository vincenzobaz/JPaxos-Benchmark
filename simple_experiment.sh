#!/bin/bash
. ./experiment.sh

output=""
replicas=3
clients=1
expname="$1"
pmaddress="localhost:9090"

# Resetting puppetmaster
resetPM $pmaddress
# Starting replicas and client
./fill_properties.sh $replicas template.properties
./experiment_cli.sh run $replicas generated-paxos.properties $clients $pmaddress
notifyPM $pmaddress "Experiment started"
sleep 10s

leader="$(get_leader)"
nonleader="$((($leader + 1) % $replicas))"

echo "=== Killing non leader ==="
kill_replica $nonleader
notifyPM $pmaddress "$nonleader killed"
sleep 1m

echo "=== Restarting non leader ==="
start_replica $nonleader
notifyPM $pmaddress "$nonleader recovered"

echo "=== Killing leader ==="
kill_replica $leader
notifyPM $pmaddress "$leader killed"
sleep 15s

echo "=== Stopping experiment ==="
notifyPM $pmaddress "End of experiment"
./experiment_cli.sh stop $replicas $clients
notifyPM $pmaddress "END"

python3 analyze_client_logs.py $output $expname

