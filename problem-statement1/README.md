Header: symbol,series,open,high,low,close,last,prevClose,totTrdQty,totTrdVal,timestamp,totalTrades,isin

Kafka
	Create:	./bin/kafka-topics.sh --bootstrap-server localhost:7092 --create --topic ps11 --partitions 1 --replication-factor 1
	List:	./bin/kafka-topics.sh --bootstrap-server localhost:7092 --list
	Count:	./bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list  localhost:7092  --topic  ps11 --time -1 --offsets 1 | awk -F ":" '{sum += $3} END {print sum'}
	
Running application
	$JAVA_HOME/bin/java -jar problem-statement1-0.0.1-SNAPSHOT.jar <nThreads>