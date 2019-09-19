package assignments.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import assignments.kafka.service.KafkaService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ashishb888
 */

@SpringBootApplication
@Slf4j
public class KafkaApp implements CommandLineRunner {

	@Autowired
	private KafkaService kafkaService;

	public static void main(String[] args) {
		SpringApplication.run(KafkaApp.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("run service");

		// args[0]: --spring.output.ansi.enabled=always
		log.info("nThreads: " + args[0]);
		kafkaService.main(Integer.valueOf(args[0]));
	}

}
