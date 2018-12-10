**`Akka Examples`** 
<code>
<br>java -Dapplication.portId=8080 -jar target/examples-akka-1.1.1-uber.jar
<br>java -Dapplication.portId=8081 -jar target/examples-akka-1.1.1-uber.jar
<br>java -Dapplication.portId=8082 -jar target/examples-akka-1.1.1-uber.jar
<code>
<br>

- Add shard region group sub group
- Akka emoting to have more actors per shardRegion
- In memory persisteence
- Loading and busy modules to handle no of items in each region group
- Move from spray to akka-http

http://allaboutscala.com/scala-frameworks/akka/
https://www.reddit.com/r/scala/comments/9mao3l/realworld_akka_architecture_and_examples/
http://prochera.com/blog/2014/07/15/building-a-scalable-and-highly-available-reactive-applications-with-akka-load-balancing-revisited/