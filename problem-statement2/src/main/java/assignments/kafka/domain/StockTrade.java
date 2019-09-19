package assignments.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StockTrade {

	private String symbol;
	private String series;
	private double open;
	private double high;
	private double low;
	private double close;
	private double last;
	private double prevClose;
	private long totTrdQty; // Maybe double
	private double totTrdVal;
	private String timestamp; // 07-JAN-2019 // can think of actual date/timestamp
	private double totalTrades;
	private String isin;

}
