#!/bin/sh

rm -rf out/*
javac -d out src/overlay/*.java
javac -d out src/streaming/*.java
if [ "$1" = "server" ]; then
	if [ "$2" = "node" ]; then
    		java -cp out overlay.NodeManager -server overlay.xml;
	elif [ "$2" = "stream" ]; then
		java -cp out streaming.OTT_Streaming -server;
	fi
elif [ "$1" = "client" ]; then
	if [ "$2" = "node" ]; then
    		java -cp out overlay.NodeManager -client;
	elif [ "$2" = "stream" ]; then
		java -cp out streaming.OTT_Streaming -client;
	fi
fi
