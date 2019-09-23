package assignments.kafka.service;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import assignments.kafka.common.AppUtils;
import assignments.kafka.common.serialization.StockTradeDeserializer;
import assignments.kafka.common.serialization.StockTradeSerializer;
import assignments.kafka.domain.StockTrade;
import assignments.kafka.properties.AppProperties;
import assignments.kafka.properties.KafkaProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ashishb888
 */

@Service
@Slf4j
@SuppressWarnings("unused")
public class KafkaStreamService {

	@Autowired
	private KafkaProperties kp;
	@Autowired
	private AppUtils appUtils;
	@Autowired
	private AppProperties ap;
	private String topic;
	private Producer<String, Double> producer;

	private void stream() throws Exception {
		log.debug("stream service");

		final StreamsBuilder builder = new StreamsBuilder();

		KStream<String, StockTrade> source = builder.stream(topic, Consumed.with(Serdes.String(),
				Serdes.serdeFrom(new StockTradeSerializer(), new StockTradeDeserializer())));

//		source.groupBy((k, v) -> v.getTimestamp()).count().toStream().to(topic + "-out",
//				Produced.with(Serdes.String(), Serdes.Long()));

		// source.groupBy((k, v) -> KeyValue.pair(k, v.getTotTrdVal()),
		// Produced.with(Serdes.String(), Serdes.Double())).aggregate(initializer,
		// adder, subtractor);

		// WORKS FINE: Putting output to a topic
		KTable<String, Double> result = source.map((k, v) -> KeyValue.pair(k, v.getTotTrdVal())).groupByKey()
				.reduce((aggVal, newVal) -> aggVal < newVal ? newVal : aggVal);

		result.toStream().foreach((k, v) -> {
			producer.send(new ProducerRecord<String, Double>(k, k, v));
		});
//		source.mapValues(StockTrade::getTotTrdVal).groupByKey()
//				.reduce((aggVal, newVal) -> aggVal < newVal ? newVal : aggVal).toStream().to(topic + "-out");

		final Topology topology = builder.build();
		final KafkaStreams streams = new KafkaStreams(topology, configs());
		final CountDownLatch latch = new CountDownLatch(1);

		log.info("topology: " + topology.describe());

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			streams.close();
			latch.countDown();
		}, "streams-shutdown-hook"));

		try {
			streams.start();
			latch.await();
		} catch (Throwable e) {
			log.error("", e);
		}
	}

	private void start() throws Exception {
		log.debug("start service");
		stream();

	}

	private void close() throws Exception {
		log.debug("close service");

		// Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	}

	private Properties configs() {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, kp.getKafkaStreams().get(StreamsConfig.APPLICATION_ID_CONFIG));
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
				kp.getKafkaStreams().get(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass());

		return props;
	}

	private void init() {
		log.debug("init service");

		topic = kp.getMetaData().get("topic");
		log.info("topic: " + topic);
		producer = appUtils.producer(kp.getKafkaProducer());
	}

	public void main() {
		log.debug("main service");

		init();
		try {
			start();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
