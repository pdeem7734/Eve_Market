package market.autotrader;

import java.util.*;
import java.math.*;

import market.database.*;
public class AdvancedTrader extends Trader {
	static enum TrendDirection {UP, DOWN, STABLE};
	private TrendDirection trendDirection;
	
	ArrayList<Integer> positiveTrendLengths = new ArrayList<Integer>();		
	ArrayList<Integer> negativeTrendLengths = new ArrayList<Integer>();
	ArrayList<Integer> stableTrendLengths = new ArrayList<Integer>();
	
	
	public AdvancedTrader(MarketData marketData){
		this.marketData = marketData;
	}
	
	private void addTrend(TrendDirection trendType, Integer length) {
		switch(trendType) {
		case UP:
			positiveTrendLengths.add(length);
			break;
		case STABLE:
			negativeTrendLengths.add(length);
			break;
		case DOWN:
			stableTrendLengths.add(length);
			break;
		default:
			throw new AssertionError("Unknown TrendType");
				
		}
	}
	
	
	@Override
	public Trade[] suggestTrades() {
		//we are going to start out with some basic trend analysis
		Trade trade1 = new Trade(new Integer(29668), "PLEX");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		
		ArrayList<TrendDirection> trendIndex = new ArrayList<TrendDirection>();
		BigDecimal averagePriceDifference;
		
		
		//we can only implement this like this because we are using a single itemID 
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(29668));
				
		String threeDaysAhead;
		Integer trendLength = new Integer(1);
		TrendDirection lastTrendDirection = TrendDirection.STABLE;
		TrendDirection curentTrendDirection = TrendDirection.STABLE;
		
		
		//this will iterate from the start date to the end date.
		//will want to compare trend information with index n and n + 3 rather then adjacent indexes
		for(String date : historicalData.keySet()) {
			try {
			//current solution is stupid but it works, so we will work with it. 
			//TODO: change to a calendar based increment solution
			threeDaysAhead = historicalData.higherKey(historicalData.higherKey(historicalData.higherKey(date)));
			
			//gets the difference between date 3 days ahead and the curen't price
			//Positive represents a rise in price
			averagePriceDifference = historicalData.get(threeDaysAhead)[4].subtract(historicalData.get(date)[4]);
			
			//test to see if it is within 1% of the original price
			if (averagePriceDifference.abs().compareTo(historicalData.get(date)[4].multiply(new BigDecimal(.01))) < 0) {
				trendIndex.add(TrendDirection.STABLE);
				
				
				lastTrendDirection = curentTrendDirection;
				curentTrendDirection = TrendDirection.STABLE;
				
				if (curentTrendDirection == lastTrendDirection) {
					trendLength ++;
				} else {
					System.out.print(trendLength + " ");
					System.out.print(lastTrendDirection);
					addTrend(lastTrendDirection, trendLength);
					trendLength = 1;
					System.out.println(" : Start Date " + date);
				}
				
				
			} else  if (averagePriceDifference.compareTo(new BigDecimal(0)) > 0) { //checks the sign, if it's positive it's an up trend
				trendIndex.add(TrendDirection.UP);
				
				lastTrendDirection = curentTrendDirection;
				curentTrendDirection = TrendDirection.UP;
				
				if (curentTrendDirection == lastTrendDirection) {
					trendLength ++;
				} else {
					System.out.print(trendLength + " ");
					System.out.print(lastTrendDirection);
					addTrend(lastTrendDirection, trendLength);
					trendLength = 1;
					System.out.println(" : Start Date " + date);
				}
				
			} else { //if it has passed these points it's a down trend
				trendIndex.add(TrendDirection.DOWN);
				
				lastTrendDirection = curentTrendDirection;
				curentTrendDirection = TrendDirection.DOWN;
				
				if (curentTrendDirection == lastTrendDirection) {
					trendLength ++;
				} else {
					System.out.print(trendLength + " ");
					System.out.print(lastTrendDirection);
					addTrend(lastTrendDirection, trendLength);
					trendLength = 1;
					System.out.println(" : Start Date " + date);
				}
			}
			} catch (NullPointerException e) {
				//TODO: add logic so this step isn't required
			}
		}
		System.out.println("Total Number of UP Trends     : " + positiveTrendLengths.size());
		System.out.println("Total Number of STABLE Trends : " + stableTrendLengths.size());
		System.out.println("Total Number of DOWN Trends   : " + negativeTrendLengths.size());
		
		return null;
	}
	
	public static void main(String[] args) {
		MySQLMarketConnection sqlCon = new MySQLMarketConnection();  
		MarketData marketData = new MarketData(sqlCon);
		AdvancedTrader adv = new AdvancedTrader(marketData);
		
		adv.suggestTrades();
	}
	
}
