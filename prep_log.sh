#!/bin/bash

echo "" > global_log.out

for logfile in logs/replica*.out
do
	filename_with_ext=$(basename "$logfile")
	actor="${filename_with_ext%.*}"

	awk -F "\t" -v actname="$actor" '{if (NF>= 2) {print actname,"\t",$0}}' $logfile >> global_log.out
done

echo "$(sort -k 2 global_log.out)" > global_log.out

