#!/bin/bash

for i in `seq 0 $(($1 - 1))`; do
	echo "Stopping replica $i"
	address="http://127.0.0.1:$((7000 + $i))/stop"
	res="$(curl -s -G $address)"
	echo "Replica replied: $res"
done

