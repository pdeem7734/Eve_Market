package market.autotrader;

import java.math.*;
import java.util.*;

public class MarketTrend {
	private ArrayList<BigDecimal> prices = new ArrayList<BigDecimal>(); 
	
	private AdvancedTrader.TrendDirection direction;
	private Integer itemID;
	private Integer length;
	
	public MarketTrend(Integer itemID, AdvancedTrader.TrendDirection direction, ArrayList<BigDecimal> prices) {
		this.prices.addAll(prices);
		this.direction = direction;
		this.itemID = itemID;
		this.length = this.prices.size();
	}
	
	public MarketTrend(MarketTrend copyTrend) {
		this.prices.addAll(copyTrend.prices);
		this.direction = copyTrend.direction;
		this.itemID = copyTrend.itemID;
		this.length = this.prices.size();
	}
	
	public ArrayList<BigDecimal> getPrices() {
		return prices;
	}
	
	public void addPrices(ArrayList<BigDecimal> pricesToAdd) {
		prices.addAll(pricesToAdd);
		length = prices.size();
	}
	
	public AdvancedTrader.TrendDirection getDirection() {
		return direction;
	}
	
	public Integer getItemID() {
		return itemID;
	}
	
	public BigDecimal getStartValue() {
		return prices.get(0);
	}
	
	public BigDecimal getEndValue() {
		return prices.get(prices.size() - 1);
	}
	
	public BigDecimal getPercentageChange() {
		return getPriceDifference().divide(getStartValue(), 10, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
	}
	
	public BigDecimal getPriceDifference() {
		return getEndValue().subtract(getStartValue());
	}
	public void printTrend() {
		switch (direction) {
		case UP:
			System.out.println("Positive " + (length) + " day trend");
			System.out.println("  Start Value       : " + getStartValue());
			System.out.println("  End Value         : " + getEndValue());
			System.out.println("  Price Difference  : " + getPriceDifference());
			System.out.println("  Percentage Change : " + getPercentageChange());
			break;
		case DOWN:
			System.out.println("Negative " + (length) + " day trend");
			System.out.println("  Start Value       : " + getStartValue());
			System.out.println("  End Value         : " + getEndValue());
			System.out.println("  Price Difference  : " + getPriceDifference());
			System.out.println("  Percentage Change : " + getPercentageChange());
			break;
		default:
			break;
		}
	}
	public void removeAllPrices() {
		prices.removeAll(prices);
		prices.trimToSize();
		length = prices.size();
	}
	
	//TODO: update to show the index at which the highest convolution occurs
	//better option is going to be to just make a new class for trend comparsion
	public BigDecimal getHighestConvolution(MarketTrend comparsionData) {
		BigDecimal highestConvolution = new BigDecimal(0);
		BigDecimal tempConvolution;
		BigDecimal[] tempArray = new BigDecimal[comparsionData.length - 1];
		BigDecimal comparsionMean;
		BigDecimal thisMean;
		Integer indexForConvolution = new Integer(0);
		
		comparsionMean = getAverage(comparsionData.prices.toArray(new BigDecimal[comparsionData.length]));
		
		for(int k = 0; k < (this.length - comparsionData.length + 1);k++) {
			System.arraycopy(prices.toArray(new BigDecimal[prices.size()]), k, tempArray, 0, comparsionData.length - 1);
			
			thisMean = getAverage(tempArray);
			//need to calculate the average of both before this step
			tempConvolution = new BigDecimal(0);
			for(int i = 0; i < tempArray.length; i++){
				tempConvolution = tempConvolution.add(comparsionData.prices.get(i).subtract(comparsionMean).multiply(tempArray[i].subtract(thisMean))); 
			}
			if(tempConvolution.compareTo(highestConvolution) > 0 || k == 0) {
				indexForConvolution = new Integer(k);
				highestConvolution = tempConvolution;
			}
			tempConvolution = null;
		}
		return highestConvolution;
	}
	
	//I really need to take the time to make a package for this crap
	private BigDecimal getAverage(BigDecimal[] arg) {
		BigDecimal avg = new BigDecimal(0);
		for (BigDecimal element : arg) {
			avg = avg.add(element);
		}
		avg = avg.divide(new BigDecimal(arg.length), 2, RoundingMode.HALF_UP);
		return avg;
	}
}
