package assignments.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import assignments.kafka.service.KafkaStreamService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ashishb888
 */

@SpringBootApplication
@Slf4j
public class KafkaStreamApp implements CommandLineRunner {

	@Autowired
	private KafkaStreamService kafkaService;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("run service");

		kafkaService.main();
	}

}
