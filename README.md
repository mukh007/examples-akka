**`Akka Examples`** 

java -Dapplication.exposed-port=8080 -jar target/examples-akka-1.1.1-uber.jar
java -Dapplication.exposed-port=8081 -jar target/examples-akka-1.1.1-uber.jar
java -Dapplication.exposed-port=8082 -jar target/examples-akka-1.1.1-uber.jar

http://allaboutscala.com/scala-frameworks/akka/
https://www.reddit.com/r/scala/comments/9mao3l/realworld_akka_architecture_and_examples/

- Add shard region group sub group
- Akka emoting to have more actors per shardRegion
- In memory persisteence
- Loading and busy modules to handle no of items in each region group
- Move from spray to akka-http