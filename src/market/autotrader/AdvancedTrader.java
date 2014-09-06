package market.autotrader;

import java.util.*;
import java.math.*;

import market.database.*;
public class AdvancedTrader extends Trader {

	public AdvancedTrader(MarketData marketData){
		this.marketData = marketData;
	}
	@Override
	public Trade[] suggestTrades() {
		//we are going to start out with some basic trend analysis
		Trade trade1 = new Trade(new Integer(34), "Trit");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		
		ArrayList<BigDecimal> positiveTrendLength = new ArrayList<BigDecimal>();		
		ArrayList<BigDecimal> negativeTrendLength = new ArrayList<BigDecimal>();
		
		
		
		//we can only implement this like this because we are using a single itemID 
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(34));
		
		//this will iterate from the start date to the end date.
		//will want to compare trend information with index n and n + 3
		//rather then adjacent indexes
		for(String date : historicalData.keySet()) {
			System.out.println(date);
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		MySQLMarketConnection sqlCon = new MySQLMarketConnection();  
		MarketData marketData = new MarketData(sqlCon);
		AdvancedTrader adv = new AdvancedTrader(marketData);
		
		adv.suggestTrades();
	}
	
}
