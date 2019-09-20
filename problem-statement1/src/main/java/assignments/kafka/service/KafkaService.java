package assignments.kafka.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@SuppressWarnings("unused")
public class KafkaService {

	private Producer<String, StockTrade> producer;
	@Autowired
	private KafkaProperties kp;
	@Autowired
	private AppUtils appUtils;
	@Autowired
	private AppProperties ap;
	private String topic;

	private void send(int nThreads) throws Exception {
		log.debug("send service");

		Set<String> files = appUtils.files(ap.getPaths().get("filesDir"));
		log.info("files: " + files);

		ExecutorService es = Executors.newFixedThreadPool(nThreads);

		files.stream().forEach(f -> {
			es.submit(() -> {
				try {
					task(f);
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			});
		});

		// es.shutdown(); // records loss
	}

	private void task(String file) throws IOException {
		log.debug("task service");
		log.debug("file: " + file);

		CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
		CsvMapper csvMapper = new CsvMapper();

		MappingIterator<StockTrade> orders = csvMapper.readerFor(StockTrade.class).with(csvSchema)
				.readValues(new File(file));

		List<StockTrade> records = orders.readAll();
		log.debug("records.size(): " + records.size());

		records.stream().forEach(r -> {
			producer.send(new ProducerRecord<String, StockTrade>(topic, r.getTimestamp(), r));
		});
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
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
