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
		
		ArrayList<Integer> positiveTrendLength = new ArrayList<Integer>();		
		ArrayList<Integer> negativeTrendLength = new ArrayList<Integer>();
		ArrayList<Integer> stableTrandLength = new ArrayList<Integer>();
		
		//we can only implement this like this because we are using a single itemID 
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(34));
		
		
		String threeDaysAhead;
		
		
		//this will iterate from the start date to the end date.
		//will want to compare trend information with index n and n + 3 rather then adjacent indexes
		for(String date : historicalData.keySet()) {
			try {
			//current solution is stupid but it works, so we will work with it. 
			threeDaysAhead = historicalData.higherKey(historicalData.higherKey(historicalData.higherKey(date)));

			if (historicalData.get(threeDaysAhead)[4].compareTo(historicalData.get(date)[4]) > 0) {
				System.out.print("UP");
			} else  if (historicalData.get(threeDaysAhead)[4].compareTo(historicalData.get(date)[4].multiply(new BigDecimal(.98))) > 0) {
				//if this is true it's with 2% south variance which i will call stable
				System.out.print("STABLE");
			} else {
				System.out.print("DOWN");
			}
			System.out.println(" : Start Date " + date);
			
			} catch (NullPointerException e) {
				//TODO: add logic so this step isn't required
			}
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
