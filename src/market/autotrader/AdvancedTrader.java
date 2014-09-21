package market.autotrader;

import java.util.*;
import java.math.*;

import market.database.*;
public class AdvancedTrader extends Trader {
	static enum TrendDirection {UP, DOWN, STABLE};
	
	ArrayList<Integer> positiveTrendLengths = new ArrayList<Integer>();		
	ArrayList<Integer> negativeTrendLengths = new ArrayList<Integer>();
	ArrayList<Integer> stableTrendLengths = new ArrayList<Integer>();
	
	
	ArrayList<TrendDirection> trendIndex = new ArrayList<TrendDirection>();
	ArrayList<BigDecimal> rollingAvgGroup = new ArrayList<BigDecimal>();
	BigDecimal averagePriceDifference;
	
	//this will be the current math location
	ArrayList<BigDecimal> currentBestFitGroup = new ArrayList<BigDecimal>();
	ArrayList<BigDecimal> previousBestFitGroup = new ArrayList<BigDecimal>();
	boolean freshTrend = true;
	
	
	ArrayList<MarketTrend> itemTrends = new ArrayList<MarketTrend>();
	MarketTrend currentMarketTrend = null;
	MarketTrend previousMarketTrend = null;
	
	//we can only implement this like this because we are using a single itemID 
	Integer trendLength = new Integer(0);
	TrendDirection lastTrendDirection = null;
	TrendDirection currentTrendDirection = null;
	
	
	public AdvancedTrader(MarketData marketData){
		this.marketData = marketData;
	}
	
	private void checkCurrentTrend(Trade trade1) {
		if (currentTrendDirection == lastTrendDirection && !freshTrend) {
			//if we are continuing a current trend
			trendLength += 7;
			currentMarketTrend.addPrices(previousBestFitGroup);
		} else if (currentTrendDirection == lastTrendDirection && freshTrend) {
			//if this is the start of a new trend
			freshTrend = false;
		} else if (lastTrendDirection == null) {
			//if this is the first trend
			//create the first market trend
			currentMarketTrend = new MarketTrend(trade1.getItemID(), currentTrendDirection, currentBestFitGroup);
			trendLength += 7;
		} else {
			ArrayList<BigDecimal> combinedIndex = new ArrayList<BigDecimal>();
			combinedIndex.addAll(previousBestFitGroup);
			combinedIndex.addAll(currentBestFitGroup);
			Integer lengthOfLastTrend;
			
			if (currentTrendDirection == TrendDirection.UP) {
				lengthOfLastTrend = getLowestIndex(combinedIndex) + 1;
			} else {
				lengthOfLastTrend = getHighestIndex(combinedIndex) + 1;
			}
			
			if (lengthOfLastTrend > previousBestFitGroup.size()) {
				//trend exists as part of the current group
				for (int k = previousBestFitGroup.size(); k < lengthOfLastTrend; k++) {
					previousBestFitGroup.add(currentBestFitGroup.get(0));
					currentBestFitGroup.remove(0);
				}
			} else {
				//trend is part of the previous group
				for(int k = previousBestFitGroup.size(); k > lengthOfLastTrend; k--) {
					currentBestFitGroup.add(0, previousBestFitGroup.get(k - 1));
					previousBestFitGroup.remove(k - 1);
				}
			}
			
			previousMarketTrend = currentMarketTrend;
			currentMarketTrend = new MarketTrend(trade1.itemID, currentTrendDirection, currentBestFitGroup);
			
			currentBestFitGroup.trimToSize();
			previousBestFitGroup.trimToSize();
			
			//if the current trend doesn't match prior trend it means the prior trend was a group of Negative
			if (freshTrend) {
				//if this originated off of a brand new trend
				previousMarketTrend.removeAllPrices();
				previousMarketTrend.addPrices(previousBestFitGroup);
				previousMarketTrend.printTrend();
			} else {
				//if this change is from an previous existing trend.
				previousMarketTrend.addPrices(previousBestFitGroup);
				previousMarketTrend.printTrend();
			}
			
			//as a change has happened we are now on a new trend. 
			freshTrend = true;
		}
	}
	
	
	@Override
	public Trade[] suggestTrades() {		
		//we are going to start out with some basic trend analysis
		Trade trade1 = new Trade(new Integer(8529), "Meta 4 Large Shield Extender");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(8529));
		
		BigDecimal currentSlope = new BigDecimal(0);
		
		//this will iterate from the start date to the end date.
		for(String date : historicalData.keySet()) {
			try {			
				//puts the elements in the current best fit group does logic if it's filled the 7 days
				currentBestFitGroup.add(historicalData.get(date)[4]);
				
				if (currentBestFitGroup.size() >= 7) {
					currentSlope = getBestFitSlope(currentBestFitGroup);
					//we are only going to test it's sign at the moment
					if (currentSlope.compareTo(new BigDecimal(0)) > 0) {
						//positive 7 day trend found
						currentTrendDirection = TrendDirection.UP;						
						checkCurrentTrend(trade1);
						
					} else {
						//negative 7 day trend found
						currentTrendDirection = TrendDirection.DOWN;
						checkCurrentTrend(trade1);
					}
					
					
					//set all current values to previous values and reset the current values
					previousBestFitGroup.clear();
					previousBestFitGroup.trimToSize();
					previousBestFitGroup.addAll(currentBestFitGroup);

					currentSlope = new BigDecimal(0);
					currentBestFitGroup.clear();
					currentBestFitGroup.trimToSize();
					
					lastTrendDirection = currentTrendDirection;
				}
				
			} catch (NullPointerException e) {
				//TODO: add logic so this step isn't required
			}
		}
		
		System.out.println("Total Number of UP Trends     : " + positiveTrendLengths.size());
		System.out.println("Total Number of STABLE Trends : " + stableTrendLengths.size());
		System.out.println("Total Number of DOWN Trends   : " + negativeTrendLengths.size());
		System.out.println("Curent Trend Length           : " + trendLength);
		System.out.println("Curent Trend Direction        : " + currentTrendDirection);
		return null;
	}

	//calculate the line of best fit
	//equation: Slope = (SumXY - SumX * YMean) / (SumX2 - SumX * XMean)
	private BigDecimal getBestFitSlope(ArrayList<BigDecimal> listToFit) {
		BigDecimal returnSlope;
		BigDecimal sumX = new BigDecimal(0);
		BigDecimal sumY = new BigDecimal(0);
		BigDecimal sumXY = new BigDecimal(0);
		BigDecimal sumX2 = new BigDecimal(0);
		BigDecimal xMean = new BigDecimal(0);
		BigDecimal yMean = new BigDecimal(0);
		
		for (int i = 0; i < listToFit.size(); i ++) {
			sumY = sumY.add(listToFit.get(i));
			sumX = sumX.add(new BigDecimal(i + 1));
			sumXY = sumXY.add(listToFit.get(i).multiply(new BigDecimal(i + 1)));
			sumX2 = sumX2.add(new BigDecimal(i + 1).multiply(new BigDecimal(i + 1)));
		}
		xMean = sumX.divide(new BigDecimal(listToFit.size()), 5);
		yMean = sumY.divide(new BigDecimal(listToFit.size()), 5);
		
		returnSlope = sumXY.subtract(sumX.multiply(yMean)).divide(sumX2.subtract(sumX.multiply(xMean)), 5);
		return returnSlope;
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
	
	private Integer getHighestIndex(ArrayList<BigDecimal> allElements) {
		Integer ret = 0;
		BigDecimal largest = allElements.get(0);
		for (int i = 0; i < allElements.size(); i ++) {
			if (largest.compareTo(allElements.get(i)) < 0) {
				largest = allElements.get(i);
				ret = i; 
			}
		}
		return ret;
	}
	
	private Integer getLowestIndex(ArrayList<BigDecimal> allElements) {
		Integer ret = 0;
		BigDecimal smallest =allElements.get(0);
		for (int i = 0; i < allElements.size(); i ++) {
			if (smallest.compareTo(allElements.get(i)) > 0) {
				smallest = allElements.get(i);
				ret = i; 
			}
		}
		return ret;
	}
	
	//entry point for testing functionality
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





/* Commented out at present may re-implement the rolling average at a later date

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
*/



