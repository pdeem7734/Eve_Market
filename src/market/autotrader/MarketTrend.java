package market.autotrader;

import java.math.*;
import java.util.*;

public class MarketTrend {
	private ArrayList<BigDecimal> prices; 
	
	private AdvancedTrader.TrendDirection direction;
	private Integer itemID;
	private Integer length;
	
	MarketTrend(Integer itemID, AdvancedTrader.TrendDirection direction, ArrayList<BigDecimal> prices) {
		this.prices = prices;
		this.direction = direction;
		this.itemID = itemID;
		this.length = prices.size();
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
	
	public BigDecimal getStart() {
		return prices.get(0);
	}
	
	public BigDecimal getEnd() {
		return prices.get(prices.size() - 1);
	}
}
