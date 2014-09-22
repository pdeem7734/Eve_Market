package market.autotrader;

import java.util.*;
import java.math.*;

import market.database.*;
public class AdvancedTrader extends Trader {
	static enum TrendDirection {UP, DOWN, STABLE};
	
	//this will be the current math location
	ArrayList<BigDecimal> currentBestFitGroup = new ArrayList<BigDecimal>();
	ArrayList<BigDecimal> previousBestFitGroup = new ArrayList<BigDecimal>();
	boolean freshTrend = true;
	
	
	ArrayList<MarketTrend> itemTrends = new ArrayList<MarketTrend>();
	MarketTrend currentMarketTrend = null;
	MarketTrend previousMarketTrend = null;
	
	TrendDirection lastTrendDirection = null;
	TrendDirection currentTrendDirection = null;
	
	
	public AdvancedTrader(MarketData marketData){
		this.marketData = marketData;
	}
	
	private void checkCurrentTrend(Trade trade1) {
		if (currentTrendDirection == lastTrendDirection && !freshTrend) {
			//if we are continuing a current trend
			currentMarketTrend.addPrices(previousBestFitGroup);
		} else if (currentTrendDirection == lastTrendDirection && freshTrend) {
			//if this is the start of a new trend
			freshTrend = false;
		} else if (lastTrendDirection == null) {
			//if this is the first trend
			//create the first market trend
			currentMarketTrend = new MarketTrend(trade1.getItemID(), currentTrendDirection, currentBestFitGroup);
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
		Trade trade1 = new Trade(new Integer(29668), "Meta 4 Large Shield Extender");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(29668));
		
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
		xMean = sumX.divide(new BigDecimal(listToFit.size()), 10, RoundingMode.HALF_UP);
		yMean = sumY.divide(new BigDecimal(listToFit.size()), 10, RoundingMode.HALF_UP);
		
		returnSlope = sumXY.subtract(sumX.multiply(yMean)).divide(sumX2.subtract(sumX.multiply(xMean)), 10, RoundingMode.HALF_UP);
		return returnSlope;
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
		avg = avg.divide(new BigDecimal(arg.length), 2, RoundingMode.HALF_UP);
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


