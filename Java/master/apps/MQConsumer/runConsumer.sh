#!/bin/bash

# Set JBoss Client JAR path (Modify this for your environment)
JBOSS_HOME=/opt/jboss-eap
JBOSS_CLIENT_JAR=$JBOSS_HOME/bin/client/jboss-client.jar

# Set Java classpath
export CLASSPATH=".:$JBOSS_CLIENT_JAR"

# Run Java Consumer
java JBossMQConsumer

java -cp "target/jboss-mq-consumer-1.0-SNAPSHOT.jar:target/dependency/*" com.example.JBossMQConsumer

export CLASSPATH="lib/*:target/MQConsumer.jar"
java -jar target/MQConsumer.jar

CLASSPATH="lib/*:target/MQConsumer.jar" java -jar target/MQConsumer.jar
