# Send messages to Kafka by REST API

Install [Apache Kafka]. 

### start zookeeper, kafka and create a topic with name "kafka-example"
Run commands in different terminals in kafka folder:
``` sh
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
bin/kafka-topics.sh --create --topic kafka-example --bootstrap-server localhost:9092 --replication-factor 1 --partitions 10
```

### start local server
Run commands in project folder (*nix):
``` sh
./sbt
```
or for Windows
``` sh
sbt.bat
```
After that in sbt terminal run server
``` sh
reStart
```
After that you will show something like this:
```
[2020-10-07 20:16:16,521] [INFO] [akka.actor.typed.ActorSystem] [HelloAkkaHttpServer-akka.actor.default-dispatcher-3] [] - Server online at http://127.0.0.1:8080/
```
Local server start on http://127.0.0.1:8080/

### Send a message to "kafka-example" topic
Send a message with key = **_my_key_** and value = _**my_value**_ to _**kafka-example**_ topic:
```
curl -H "Content-type: application/json" -X POST -d '{"key": "my_key", "value": "my_value"}' http://localhost:8080/kafka/send/kafka-example
```

### Frameworks documentation
- [Apache Kafka]
- [Akka HTTP]

[Apache Kafka]: https://kafka.apache.org/documentation/#quickstart
[Akka HTTP]: https://developer.lightbend.com/guides/akka-http-quickstart-scala/index.html