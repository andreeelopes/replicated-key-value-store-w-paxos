#!/usr/bin/env bash

nClients=$1
nReplicas=$2

mkdir -p logs/$nClients$nReplicas/clients
mkdir -p logs/$nClients$nReplicas/replicas

echo "clients = $nClients | replicas = $nReplicas"

clientStartPort=70
replicasStartPort=100

echo "deploying Rendezvous at 127.0.0.1 : 69"
java -cp replicated-key-value-store-w-paxos-assembly-0.1.jar RendezvousMain $nReplicas > logs/$nClients$nReplicas/rendezvous.log &

for i in $(seq 1 $nClients)
do
    echo "deploying Client $i at 127.0.0.1 : $(($clientStartPort+i))"
    java -cp replicated-key-value-store-w-paxos-assembly-0.1.jar ClientMain 127.0.0.1 $((clientStartPort+i)) > logs/$nClients$nReplicas/clients/client$((clientStartPort+i)).log &
done



for j in $(seq 1 $nReplicas)
do
	echo "deploying Replica $j at 127.0.0.1 : $((replicasStartPort+j))"
	java -cp replicated-key-value-store-w-paxos-assembly-0.1.jar ReplicaMain 127.0.0.1 $((replicasStartPort+j)) > logs/$nClients$nReplicas/replicas/replica$((replicasStartPort+j)).log &
done
