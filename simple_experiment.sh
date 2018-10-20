#!/bin/bash
. ./experiment.sh

output=""
reps=3
cls=5

./fill_properties.sh $reps template.properties
./experiment_cli.sh run $reps generated-paxos.properties $cls
output="$reps $cls $(date +%s%3N)"

sleep 30s

kill_replica 1
output="$output $(date +%s%3N)"

sleep 20s

start_replica 1
output="$output $(date +%s%3N)"

sleep 15s

echo "Stopping experiment"
output="$output $(date +%s%3N)"
./experiment_cli.sh stop $reps $cls
output="$output $(date +%s%3N)"

echo $output
python3 analyze_client_logs.py $output

