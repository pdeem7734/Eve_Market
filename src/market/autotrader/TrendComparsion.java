package market.autotrader;

import java.util.*;
import java.math.*;


public class TrendComparsion {
	ArrayList<MarketTrend> trendGroup1 = new ArrayList<MarketTrend>();
	MarketTrend comparsionTrend;
	
	public TrendComparsion(MarketTrend comparsionTrend) {
		this.comparsionTrend = comparsionTrend;
	}
	
	public void addTrendToComare(MarketTrend trendToAdd) {
		trendGroup1.add(trendToAdd);
	}
	
	public void addTrendsToCompare(MarketTrend[] trends){
		for (MarketTrend trend : trends) {
			trendGroup1.add(trend);
		}
	}
}
