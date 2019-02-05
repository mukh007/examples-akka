**`Akka Examples`** 
<h2>Startup</h2>
<code>
java -Dapplication.portId=8080 -jar target/examples-akka-1.1.1-uber.jar<br>
java -Dapplication.portId=8081 -jar target/examples-akka-1.1.1-uber.jar<br>
java -Dapplication.portId=8082 -jar target/examples-akka-1.1.1-uber.jar
</code>
<br>
<h2>Usage</h2>
<code>cat src/main/resources/URLs.txt | parallel -j 5 'ab -ql -n 2000 -c 1 -k {}' | grep 'Requests per second'</code>
<br><br>
- Add shard region group sub group
- Akka emoting to have more actors per shardRegion
- In memory persisteence
- Loading and busy modules to handle no of items in each region group
- Move from spray to akka-http

http://allaboutscala.com/scala-frameworks/akka/
https://www.reddit.com/r/scala/comments/9mao3l/realworld_akka_architecture_and_examples/
http://prochera.com/blog/2014/07/15/building-a-scalable-and-highly-available-reactive-applications-with-akka-load-balancing-revisited/