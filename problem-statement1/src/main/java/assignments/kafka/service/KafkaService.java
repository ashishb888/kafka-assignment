package assignments.kafka.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import assignments.kafka.common.AppUtils;
import assignments.kafka.domain.StockTrade;
import assignments.kafka.properties.AppProperties;
import assignments.kafka.properties.KafkaProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ashishb888
 */

@Service
@Slf4j
public class KafkaService {

	private Producer<String, StockTrade> producer;
	@Autowired
	private KafkaProperties kp;
	@Autowired
	private AppUtils appUtils;
	@Autowired
	private AppProperties ap;
	private String topic;

	/*
	 * Get all files from source directory and call task method for each file
	 */
	private void send(int nThreads) throws Exception {
		log.debug("send service");

		Set<String> files = appUtils.files(ap.getPaths().get("filesDir"));
		log.debug("files: " + files);

		ExecutorService es = Executors.newFixedThreadPool(nThreads);
		CountDownLatch latch = new CountDownLatch(files.size());

		files.stream().forEach(f -> {
			es.submit(() -> {
				try {
					task(f, latch);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			});
		});

		latch.await();
		producer.close();
		es.shutdown();
	}

	/*
	 * Read data files, deserialise records to StockTrade object and send them to
	 * the Kafka as JSON
	 */
	private void task(String file, CountDownLatch latch) throws IOException {
		log.debug("task service");
		log.debug("file: " + file);

		CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
		CsvMapper csvMapper = new CsvMapper();
		// To avoid Unrecognized field "" error, it was coming for trailing ,
		csvMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		MappingIterator<StockTrade> orders = csvMapper.readerFor(StockTrade.class).with(csvSchema)
				.readValues(new File(file));

		List<StockTrade> records = orders.readAll();
		log.debug("records.size(): " + records.size());

		records.stream().forEach(r -> {
			producer.send(new ProducerRecord<String, StockTrade>(topic, r.getTimestamp(), r));
		});

		latch.countDown();
	}

	private void start(int nThreads) throws Exception {
		log.debug("start service");

		send(nThreads);
	}

	private void close() throws Exception {
		log.debug("close service");

		Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
	}

	private void init() {
		log.debug("init service");

		topic = kp.getMetaData().get("topic");
		log.info("topic: " + topic);

		producer = appUtils.producer(kp.getKafkaProducer());
	}

	public void main(int nThreads) {
		log.debug("main service");

		init();
		try {
			start(nThreads);
			close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
