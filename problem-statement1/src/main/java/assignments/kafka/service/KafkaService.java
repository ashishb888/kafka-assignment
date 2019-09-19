package assignments.kafka.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.producer.Producer;
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

		es.shutdown();
	}

	private void task(String file) throws IOException {
		log.debug("task service");
		CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
		CsvMapper csvMapper = new CsvMapper();

		MappingIterator<StockTrade> orders = csvMapper.readerFor(StockTrade.class).with(csvSchema)
				.readValues(new File(file));

		orders.readAll().stream().limit(10).forEach(r -> {
			log.info("r: " + r);
		});

//		try (Reader reader = Files.newBufferedReader(file)) {
//			Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.withDelimiter(',').withQuote(null).withHeader(
//					"SYMBOL,SERIES,OPEN,HIGH,LOW,CLOSE,LAST,PREVCLOSE,TOTTRDQTY,TOTTRDVAL,TIMESTAMP,TOTALTRADES,ISIN,")
//					.parse(reader);
//			for (CSVRecord csvRecord : csvRecords) {
//				StockTrade st = new StockTrade();
//				st.setSymbol("SYMBOL");
//				st.setSeries("SERIES");
//			}
//		}

	}

	private void start(int nThreads) throws Exception {
		log.debug("start service");

//		ExecutorService es = Executors.newFixedThreadPool(2);
//
//		for (int i = 0; i < 10; i++) {
//			int li = i;
//			es.submit(() -> {
//				log.info(li + "started");
//				try {
//					Thread.sleep(10000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				log.info(li + "ended");
//			});
//		}
//
//		es.shutdown();
		send(nThreads);
	}

	private void init() {
		log.debug("init service");

		// producer();
		// producer = appUtils.producer(kp.getKafkaProducer());
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
