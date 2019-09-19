package assignments.kafka.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.stereotype.Component;

import assignments.kafka.domain.StockTrade;

@Component
public class AppUtils {

	public Set<String> files(String dir) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toAbsolutePath).map(Path::toString)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new IOException("Error in listing files", e);
		}
	}

	public Set<Path> paths(String dir) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::toAbsolutePath)
					.collect(Collectors.toSet());
		} catch (IOException e) {
			throw new IOException("Error in listing files", e);
		}
	}

	public Producer<String, StockTrade> producer(Map<String, String> map) {
		Properties configs = new Properties();
		configs.putAll(map);
		return new KafkaProducer<>(configs);
	}
}
