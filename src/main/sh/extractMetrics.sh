#!/usr/bin/env bash

dir=$1/clients

mkdir $dir/../metrics


for i in $dir/*
do
	s=$(awk '/latency/' $i)
	s=${s//latency: /}
	s=${s//throughput: /}
	s=${s// ms/}
	s=${s//ops\/s/}
	echo "$s" >> $dir/../metrics/metrics.log
done

echo "Completed."