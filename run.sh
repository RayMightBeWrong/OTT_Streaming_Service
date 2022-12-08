#!/bin/sh

rm -rf out/*
javac -d out src/overlay/*.java src/overlay/TCP/*.java src/overlay/state/*.java src/overlay/bootstrapper/*.java src/streaming/*.java

if [ "$1" = "bstrapper" ]; then
    	java -cp out overlay.NodeManager config overlay.xml;
fi
if [ "$1" = "node" ]; then
    	java -cp out overlay.NodeManager "$2";
fi
if [ "$1" = "stream" ]; then
	java -cp out streaming.OTTStreaming;
fi
