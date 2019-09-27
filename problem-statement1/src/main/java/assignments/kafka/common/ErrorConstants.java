package assignments.kafka.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorConstants {

	public static String NO_ARGS = "Please pass <nThread> as an argument";
	public static String AT_LEAST_ONE = "You must provide at least one thread in order to proceed";
	public static String INVALID = "Please provide a valid integer";
}
