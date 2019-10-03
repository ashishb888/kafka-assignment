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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import assignments.kafka.common.AppUtils;
import assignments.kafka.common.serialization.StockTradeDeserializer;
import assignments.kafka.common.serialization.StockTradeSerializer;
import assignments.kafka.domain.StockTrade;
import assignments.kafka.properties.KafkaProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ashishb888
 */

@Service
@Slf4j
public class KafkaStreamService {

	@Autowired
	private KafkaProperties kp;
	@Autowired
	private AppUtils appUtils;
	private String topic;
	private Producer<String, Double> producer;

	/*
	 * This method start a streaming on provided source topic. And group the records
	 * by timestamp and find the max total trade val for each group
	 */
	private void stream() throws Exception {
		log.debug("stream service");

		final StreamsBuilder builder = new StreamsBuilder();

		KStream<String, StockTrade> source = builder.stream(topic, Consumed.with(Serdes.String(),
				Serdes.serdeFrom(new StockTradeSerializer(), new StockTradeDeserializer())));

		KTable<String, Double> result = source.map((k, v) -> KeyValue.pair(k, v.getTotTrdVal())).groupByKey()
				.reduce((aggVal, newVal) -> aggVal < newVal ? newVal : aggVal);

		result.toStream().foreach((k, v) -> {
			producer.send(new ProducerRecord<String, Double>(k, k, v));
		});

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

	private Properties configs() {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, kp.getKafkaStreams().get(StreamsConfig.APPLICATION_ID_CONFIG));
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
				kp.getKafkaStreams().get(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG));
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass());

		return props;
	}

	/*
	 * Set topic name. Create a producer
	 */
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
