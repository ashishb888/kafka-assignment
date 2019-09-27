package assignments.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static assignments.kafka.common.ErrorConstants.*;
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

		if (args.length != 1) {
			log.error(NO_ARGS);
			System.exit(1);
		}

		log.info("nThreads: " + args[0]);

		try {
			int nThreads = Integer.valueOf(args[0]);

			if (nThreads < 1) {
				log.error(AT_LEAST_ONE);
				System.exit(1);
			}

			kafkaService.main(nThreads);
		} catch (NumberFormatException e) {
			log.error(INVALID);
			System.exit(1);
		}

	}

}
