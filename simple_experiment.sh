#!/bin/bash
. ./experiment.sh
export _JAVA_OPTIONS="-ea"

output=""
reps=3
cls=5

./fill_properties.sh $reps template.properties
./experiment_cli.sh run $reps generated-paxos.properties $cls
output="$reps $cls $(date +%s%3N)"

sleep 1m

kill_replica 0
output="$output $(date +%s%3N)"

sleep 1m

start_replica 0
output="$output $(date +%s%3N)"

sleep 30s

kill_replica 0
output="$output $(date +%s%3N)"

sleep 30s

start_replica 0
output="$output $(date +%s%3N)"

sleep 30s

echo "Stopping experiment"
output="$output $(date +%s%3N)"
./experiment_cli.sh stop $reps $cls
output="$output $(date +%s%3N)"

echo $output
python3 analyze_client_logs.py $output

