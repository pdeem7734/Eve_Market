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
}
