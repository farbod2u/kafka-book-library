# Getting Started

### Build application
```
$ mvn clean package
```
or to build without run test
```
$ mvn clean package -DskipTest
```


### Get Server Info
```
$ ./kafka-topics.sh --bootstrap-server localhost:9092 --list
```
```
$ ./kafka-topics.sh --bootstrap-server localhost:9092 --topic book-lib-event --describe
```
```
$ ./kafka-configs.sh --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name book-lib-event --add-config min.insync.replicas=2
```