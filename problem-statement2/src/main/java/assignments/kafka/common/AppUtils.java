package assignments.kafka.common;

import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import assignments.kafka.domain.StockTrade;

@Component
public class AppUtils {

	public Producer<String, StockTrade> producer(Map<String, String> map) {
		Properties configs = new Properties();
		configs.putAll(map);
		return new KafkaProducer<>(configs);
	}
}
