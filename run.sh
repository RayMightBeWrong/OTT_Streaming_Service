#!/bin/sh

rm -rf out/*
javac -d out src/overlay/*.java
javac -d out src/streaming/*.java
if [ "$1" = "bstrapper" ]; then
    	java -cp out overlay.NodeManager config overlay.xml;
fi
if [ "$1" = "node" ]; then
    	java -cp out overlay.NodeManager "$2";
fi
if [ "$1" = "stream" ]; then
	java -cp out overlay.NodeManager stream;
fi

#if [ "$1" = "stream" ]; then
#	if [ "$2" = "server" ]; then
#		java -cp out streaming.OTT_Streaming -server;
#	elif [ "$2" = "client" ]; then
#		java -cp out streaming.OTT_Streaming -client;
#	fi
#fi
