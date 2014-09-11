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
	
	//should probably create my own class stuff for simple things like this
	private Integer getAverage(Integer[] arg) {
		Integer avg = 0;
		for (Integer element : arg) {
			avg += element;
		}
		avg = avg/arg.length;
		return avg;
	}
	private BigDecimal getAverage(BigDecimal[] arg) {
		BigDecimal avg = new BigDecimal(0);
		for (BigDecimal element : arg) {
			avg = avg.add(element);
		}
		avg = avg.divide(new BigDecimal(arg.length), 2);
		return avg;
	}
	
	@Override
	public Trade[] suggestTrades() {
		//we are going to start out with some basic trend analysis
		Trade trade1 = new Trade(new Integer(8529), "PLEX");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		
		ArrayList<TrendDirection> trendIndex = new ArrayList<TrendDirection>();
		ArrayList<BigDecimal> rollingAvgGroup = new ArrayList<BigDecimal>();
		BigDecimal averagePriceDifference;
		
		
		//we can only implement this like this because we are using a single itemID 
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(8529));
		
		Integer trendLength = new Integer(1);
		TrendDirection lastTrendDirection = TrendDirection.STABLE;
		TrendDirection curentTrendDirection = TrendDirection.STABLE;
		
		
		//this will iterate from the start date to the end date.
		for(String date : historicalData.keySet()) {
			try {
				//groups the elements of rolling average in an array list
				if (rollingAvgGroup.size() < 7) {
					rollingAvgGroup.add(historicalData.get(date)[4]);
				} else {
					rollingAvgGroup.remove(0);
					rollingAvgGroup.add(historicalData.get(date)[4]);
				}
				BigDecimal rollingAverage = getAverage(rollingAvgGroup.toArray(new BigDecimal[rollingAvgGroup.size()]));
				
				
				//gets the difference between date 3 days ahead and the curen't price
				//Positive represents a rise in price
				averagePriceDifference = rollingAverage.subtract(rollingAvgGroup.get(rollingAvgGroup.size() - 1));
				
				//test to see if it is within 1% of the rolling average price
				if (averagePriceDifference.abs().compareTo(rollingAverage.multiply(new BigDecimal(.01))) < 0) {
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
						System.out.println(" : End Date " + date);
					}
					
				} else  if (averagePriceDifference.compareTo(new BigDecimal(0)) < 0) {
					//checks the sign, if it's negative current price is above the moving average
					//price is on an up trend
					
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
						System.out.println(" : End Date " + date);
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
						System.out.println(" : End Date " + date);
					}
				}
			} catch (NullPointerException e) {
				//TODO: add logic so this step isn't required
			}
		}
		System.out.println("Total Number of UP Trends     : " + positiveTrendLengths.size());
		System.out.println("Total Number of STABLE Trends : " + stableTrendLengths.size());
		System.out.println("Total Number of DOWN Trends   : " + negativeTrendLengths.size());
		System.out.println("Curent Trend Length           : " + trendLength);
		System.out.println("Curent Trend Direction        : " + curentTrendDirection);
		return null;
	}
	
	public static void main(String[] args) {
		MySQLMarketConnection sqlCon = new MySQLMarketConnection();
		if (sqlCon.testConnection()) {
			MarketData marketData = new MarketData(sqlCon);
			AdvancedTrader adv = new AdvancedTrader(marketData);
			adv.suggestTrades();
		} else {
			System.out.println("Unable to connect");
		}
		
		
	}
	
}
