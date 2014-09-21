package market.autotrader;

import java.util.*;
import java.math.*;

import market.database.*;
public class AdvancedTrader extends Trader {
	static enum TrendDirection {UP, DOWN, STABLE};
	
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
	
	
	@Override
	public Trade[] suggestTrades() {
		//we are going to start out with some basic trend analysis
		Trade trade1 = new Trade(new Integer(8529), "Meta 4 Large Shield Extender");
		marketData.loadCrestInfo(425, new Trade[] {trade1});
		
		ArrayList<TrendDirection> trendIndex = new ArrayList<TrendDirection>();
		ArrayList<BigDecimal> rollingAvgGroup = new ArrayList<BigDecimal>();
		BigDecimal averagePriceDifference;
		
		//this will be the current math location
		ArrayList<BigDecimal> currentBestFitGroup = new ArrayList<BigDecimal>();
		ArrayList<BigDecimal> previousBestFitGroup = new ArrayList<BigDecimal>();
		BigDecimal currentSlope = new BigDecimal(0);
		BigDecimal previousSlope = new BigDecimal(0);
		boolean freshTrend = true;
		
		
		//we can only implement this like this because we are using a single itemID 
		TreeMap<String, BigDecimal[]> historicalData = marketData.crestData.get(new Integer(8529));
		Integer trendLength = new Integer(0);
		TrendDirection lastTrendDirection = null;
		TrendDirection currentTrendDirection = null;
		
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
				
				//puts the elements in the current best fit group does logic if it's filled the 7 days
				currentBestFitGroup.add(historicalData.get(date)[4]);
				
				if (currentBestFitGroup.size() >= 7) { //we have filled the 7 elements
					//calculate the line of best fit
					//equation: Slope = (SumXY - SumX * YMean) / (SumX2 - SumX * XMean)
					BigDecimal sumX = new BigDecimal(0);
					BigDecimal sumY = new BigDecimal(0);
					BigDecimal sumXY = new BigDecimal(0);
					BigDecimal sumX2 = new BigDecimal(0);
					BigDecimal xMean = new BigDecimal(0);
					BigDecimal yMean = new BigDecimal(0);
					
					for (int i = 0; i < currentBestFitGroup.size(); i ++) {
						sumY = sumY.add(currentBestFitGroup.get(i));
						sumX = sumX.add(new BigDecimal(i + 1));
						sumXY = sumXY.add(currentBestFitGroup.get(i).multiply(new BigDecimal(i + 1)));
						sumX2 = sumX2.add(new BigDecimal(i + 1).multiply(new BigDecimal(i + 1)));
					}
					xMean = sumX.divide(new BigDecimal(currentBestFitGroup.size()), 5);
					yMean = sumY.divide(new BigDecimal(currentBestFitGroup.size()), 5);
					
					currentSlope = sumXY.subtract(sumX.multiply(yMean)).divide(sumX2.subtract(sumX.multiply(xMean)), 5);
					//we are only going to test it's sign at the moment
					if (currentSlope.compareTo(new BigDecimal(0)) > 0) {
						//positive 7 day trend found
						lastTrendDirection = currentTrendDirection;
						currentTrendDirection = TrendDirection.UP;
						if (currentTrendDirection == lastTrendDirection && !freshTrend ) {
							//if we are continuing a current trend
							trendLength += 7;
						} else if (currentTrendDirection == lastTrendDirection && freshTrend ) {
							//if this is the start of a new trend
							freshTrend = false;
						} else if (lastTrendDirection == null) {
							//if this is the first trend							
						} else {
							ArrayList<BigDecimal> combinedIndex = new ArrayList<BigDecimal>();
							combinedIndex.addAll(previousBestFitGroup);
							combinedIndex.addAll(currentBestFitGroup);
							
							Integer lengthOfLastTrend = getLowestIndex(combinedIndex) + 1;
							addTrend(lastTrendDirection, trendLength);
							
							if (lengthOfLastTrend > previousBestFitGroup.size()) {
								//trend exists as part of the current group
								for (int k = previousBestFitGroup.size(); k < lengthOfLastTrend; k++) {
									currentBestFitGroup.remove(0);
								}
							} else {
								//trend is part of the previous group
								for(int k = previousBestFitGroup.size(); k > lengthOfLastTrend; k--) {
									currentBestFitGroup.add(0, previousBestFitGroup.get(k - 1));
								}
							}
							
							currentBestFitGroup.trimToSize();
							previousBestFitGroup.trimToSize();
							Integer totalLengthOfPriorTrend;
							
							//if the current trend doesn't match prior trend it means the prior trend was a group of Negative
							if (freshTrend) {
								//if this originated off of a brand new trend
								totalLengthOfPriorTrend = lengthOfLastTrend;
								System.out.println("Negative " + (totalLengthOfPriorTrend) + " day Trend Ending : " + date);
							} else {
								//if this change is from an previous exsisting trend. 
								totalLengthOfPriorTrend = trendLength + lengthOfLastTrend;
								System.out.println("Negaive " + (totalLengthOfPriorTrend) + " day Trend Ending : " + date);
							}
							
							
							//as a change has happened we are now on a new trend. 
							freshTrend = true;
							
							//TODO: this will need to be altered
							trendLength = 7 + (previousBestFitGroup.size() - lengthOfLastTrend);
						}
					} else {
						//negative 7 day trend found
						lastTrendDirection = currentTrendDirection;
						currentTrendDirection = TrendDirection.DOWN;
						if (currentTrendDirection == lastTrendDirection && !freshTrend ) {
							//if we are continuing a current trend
							trendLength += 7;
						} else if (currentTrendDirection == lastTrendDirection && freshTrend ) {
							//if this is the start of a new trend
							freshTrend = false;
						} else if (lastTrendDirection == null) {
							//if this is the first trend 							
						}else {
							ArrayList<BigDecimal> combinedIndex = new ArrayList<BigDecimal>();
							combinedIndex.addAll(previousBestFitGroup);
							combinedIndex.addAll(currentBestFitGroup);
							
							Integer lengthOfLastTrend = getHighestIndex(combinedIndex) + 1;
							addTrend(lastTrendDirection, trendLength);
							
							if (lengthOfLastTrend > previousBestFitGroup.size()) {
								//trend exists as part of the current group
								for (int k = previousBestFitGroup.size(); k < lengthOfLastTrend; k++) {
									currentBestFitGroup.remove(0);
								}
							} else {
								//trend is part of the previous group
								for(int k = previousBestFitGroup.size(); k > lengthOfLastTrend; k--) {
									currentBestFitGroup.add(0, previousBestFitGroup.get(k - 1));
								}
							}
							
							currentBestFitGroup.trimToSize();
							previousBestFitGroup.trimToSize();
							Integer totalLengthOfPriorTrend;
							
							//if the current trend doesn't match prior trend it means the prior trend was a group of positives
							if (freshTrend) {
								//if this originated off of a brand new trend
								totalLengthOfPriorTrend = lengthOfLastTrend;
								System.out.println("Positive " + (totalLengthOfPriorTrend) + " day Trend Ending : " + date);
							} else {
								//if this change is from an previous exsisting trend. 
								totalLengthOfPriorTrend = trendLength + lengthOfLastTrend;
								System.out.println("Positive " + (totalLengthOfPriorTrend) + " day Trend Ending : " + date);
							}
							
							
							//as a change has happened we are now on a new trend. 
							freshTrend = true;
							
							//TODO: this will need to be altered
							trendLength = 7 + (previousBestFitGroup.size() - lengthOfLastTrend);
						}
					}
					
					
					//set all current values to previous values and reset the current values
					previousBestFitGroup.clear();
					previousBestFitGroup.trimToSize();
					previousBestFitGroup.addAll(currentBestFitGroup);
					previousSlope = currentSlope;
					currentSlope = new BigDecimal(0);
					currentBestFitGroup.clear();
					currentBestFitGroup.trimToSize();
				}
				//gets the difference between date 3 days ahead and the curen't price
				//Positive represents a rise in price
				averagePriceDifference = rollingAverage.subtract(rollingAvgGroup.get(rollingAvgGroup.size() - 1));
				
				
				
				
				
				
				
				
				
				
				/* Commented out at present may re-implement the rolling average at a later date
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
