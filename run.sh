#!/bin/sh

rm -rf out/*
javac -d out src/*.java
if [ "$1" = "server" ]; then
    java -cp out EchoServer
elif [ "$1" = "client" ]; then
    java -cp out EchoClient
fi