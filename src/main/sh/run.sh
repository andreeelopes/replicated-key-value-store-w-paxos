#!/usr/bin/env bash

nNodes=$1
ip=$2
port=$3

echo "deploying Rendezvous at 127.0.0.1:69"
java -cp replicated-key-value-store-w-paxos-assembly-0.1 127.0.0.1 69 > rendezvous.log 2 &

for i in $(seq 1 $nClients)
do
    java -jar replicated-key-value-store-w-paxos-assembly-0.1 127.0.0.1
done

for run in $(seq 2 $nNodes)
do
	let myPort=$((port+run))
	echo "deploying Node $run at $ip : $myPort"
	java -jar ./pub-sub-HyParView-assembly-0.1.jar $ip $myPort $ip $port > out$myPort.log 2 &
done
