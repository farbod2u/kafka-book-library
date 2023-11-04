# Read Me First



### build app
```
$ mvn clean package
```
or to build without run test:
```
$ mvn clean package -DskipTests
```
### run as multiple instance
```
$ cd target
```
```
$ java -jar ./kafka-consumer.jar
```
or to run on specific server port run this:
```
$ java -jar -Dserver.port=8082 ./kafka-consumer.jar
```