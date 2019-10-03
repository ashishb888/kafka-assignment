# kafka-assignment

> I have used Project Lombok, you may want to refer [this link](https://projectlombok.org/setup/eclipse) to configure Project Lombok in Eclipse

## Problem statement 1

###### Description
This application reads files from a directory, deserialise records to to StockTrade object and send them to the Kafka as JSON

###### Importing
It is a Maven based application, you can import it as a Maven project into an IDE.

###### Configuration
All the configuration is defined in application.yml file.  You need to change follwing
<pre>
bootstrap.servers
topic
appfilesDir
</pre>

###### Packaging
`mvn package`

###### Running
`$JAVA_HOME/bin/java -jar problem-statement1-0.0.1-SNAPSHOT.jar nThreads`

## Problem statement 2

###### Description
Once you finished with `Problem statement 1` you can start this application. Final result contains `timestamp` and `tottrdval`, and I have to send each record to a different topic. So I have used `timestamp` as topic name and put `tottrdval` to it.

###### Importing
It is a Maven based application, you can import it as a Maven project into an IDE.

###### Configuration
All the configuration is defined in application.yml file.  You need to change follwing
<pre>
bootstrap.servers
topic
</pre>

###### Packaging
`mvn package`

###### Running
`$JAVA_HOME/bin/java -jar problem-statement2-0.0.1-SNAPSHOT.jar `


###### Technical details
<pre>
Language: Java 8
Framework: Spring boot (2.1.6.RELEASE)
Build system: Maven (3.2+)
</pre>
