### To build project:
 - sbt assembly
 
 ### To run project:
 - java -jar server/target/zipkin-jaxrs-2.0-server-assembly-0.0.1-SNAPSHOT.jar 
 - java -jar client/target/zipkin-jaxrs-2.0-client-assembly-0.0.1-SNAPSHOT.jar 
 
 ### Or, alternatively:
 - sbt 'project server' 'run-main com.example.server.ServerStarter'
 - sbt 'project client' 'run-main com.example.client.ClientStarter'
 
 ### To run Apache Zookeeper (assuming it's downloaded and unpacked)
 - cp zookeeper-3.4.5/conf/zoo_sample.cfg zookeeper-3.4.5/conf/zoo.cfg
 - zookeeper-3.4.5/bin/zkServer.sh start
 
 ### To run Zipkin (assuming it's cloned from https://github.com/twitter/zipkin):
 - bin/collector
 - bin/query
 - bin/web