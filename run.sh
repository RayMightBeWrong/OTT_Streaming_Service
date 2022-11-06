#!/bin/sh

rm -rf out/*
javac -d out src/overlay/*.java
javac -d out src/streaming/*.java
if [ "$1" = "node" ]; then
	if [ "$2" = "server" ]; then
    		java -cp out overlay.NodeManager -server "$3";
	elif [ "$2" = "client" ]; then
    		java -cp out overlay.NodeManager -client "$3";
	fi
elif [ "$1" = "stream" ]; then
	if [ "$2" = "server" ]; then
		java -cp out streaming.OTT_Streaming -server;
	elif [ "$2" = "client" ]; then
		java -cp out streaming.OTT_Streaming -client;
	fi
fi
