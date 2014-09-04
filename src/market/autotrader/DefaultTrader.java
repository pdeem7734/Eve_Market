package market.autotrader;

import java.math.*;
import java.util.*;


public class DefaultTrader extends Trader {
	public DefaultTrader(MarketData marketData) {
		this.marketData = marketData;
	}
	
	//TODO: string[][] will be replaced with a trade class that will contain similar information
	@Override
	public Trade[] suggestTrades() {
		
		marketData.loadItemIDs();

		
		Trade[] finalTrades;
		Trade[] potentialTrades = new Trade[marketData.itemIDs.size()];
		
		int tempIndex = 0;
		for (Integer itemID : marketData.itemIDs.keySet()) {
			potentialTrades[tempIndex] = new Trade(itemID, marketData.itemIDs.get(itemID));
			tempIndex ++;
		}
		
		//search and validate
		potentialTrades = searchOrders(potentialTrades);		
		potentialTrades = validateByVolume(potentialTrades);
				
		
		//validate trades against historical trending 
		finalTrades = validateByTrend(potentialTrades);
		potentialTrades = null;
		
		return finalTrades;
	}
	
	
	//this method will check the items listed and ensure there is not a significant downward trend contained in the data loaded
	private Trade[] validateByTrend(Trade[] trades) {
		marketData.loadCrestInfo(10, trades);
		ArrayList<Trade> validatedTrades = new ArrayList<Trade>();
		
		for (Trade trade: trades){
			Integer itemID = trade.getItemID();
			try {
				ArrayList<BigDecimal> priceDifferences = new ArrayList<BigDecimal>();
				//calculates the average average across the days in question.
				//as well as the difference between each days average and the current days average
				//zeroth index will be the most recent day
				BigDecimal average = new BigDecimal(0);
				BigDecimal mostRecentDayAverage = marketData.crestData.get(itemID).lastEntry().getValue()[4];
				//this will need to be reworked
				for (BigDecimal[] big : marketData.crestData.get(itemID).values()) {
					average = average.add(big[0]);
					
					//most recent average - this average 
					priceDifferences.add(mostRecentDayAverage.subtract(big[0]));
				}
				average = average.divide(new BigDecimal(marketData.crestData.get(itemID).values().size()), 4);
				
				//calculate average change -1 because the price difference contained today - today
				BigDecimal averageChange = new BigDecimal(0);
				for (BigDecimal big: priceDifferences) {
					averageChange = averageChange.add(big);
				}
				averageChange = averageChange.divide(new BigDecimal(priceDifferences.size() - 1), 4);

				//item has been validated
				if (averageChange.compareTo(average.multiply(new BigDecimal(-.05))) > 0) {
					validatedTrades.add(trade);
				}
			} catch (Exception e) {
				//unable to validate item for one reason or another 
				e.printStackTrace();
				break;
			}
		}
		
		return validatedTrades.toArray(new Trade[validatedTrades.size()]);
	}
	
	//validates that the trades passed to it have sufficent volume and return to be worth investing in.
	private Trade[] validateByVolume(Trade[] trades) {
		marketData.loadMetaMap(trades);
		BigDecimal[] curentItemData;
		BigDecimal volume;
		BigDecimal iskByVolume;
		HashSet<Trade> validatedTrades = new HashSet<Trade>();

		//iterate though all passed item ID's
		for (Trade trade : trades) {
			Integer itemID = trade.getItemID();
			curentItemData = marketData.metaData.get(itemID);
			try {

				//gets the profit in isk multiplied by 5% of the total market volume over 24h
				//5% might be a bit high for expected market impact but will adjust as needed
				volume = curentItemData[8];	
				trade.setVolume(volume);
				
				iskByVolume = trade.getProfitByVolume().multiply(new BigDecimal(.05));
				
				//if the volume is over 200 items traded in the past 24h
				//and the isk by 5%volume is over 10m validate the trade				
				if (volume.compareTo(new BigDecimal("200")) > 0 && (iskByVolume.compareTo(new BigDecimal(5_000_000)) > 0)) {
					validatedTrades.add(trade);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//return all validated trades
		return validatedTrades.toArray(new Trade[validatedTrades.size()]);
	}
	
	//searches though the itemID's passed to it looking for recommended trades
	private Trade[] searchOrders(Trade[] trades) {
		marketData.loadOrderMap(trades);
		BigDecimal[] curentItemData;
		BigDecimal profitPercent;
		BigDecimal profitISK;
		
		//this is the item ID for the possible trades 
		HashSet<Trade> possibleTrades = new HashSet<Trade>();
		
		//cycles though all item ID's passed to it 
		for (Trade trade : trades) {
			Integer itemID = trade.getItemID();
			try {
				curentItemData = new BigDecimal[] {marketData.buyOrders.get(itemID), marketData.sellOrders.get(itemID)};
				
				//index 0 sell orders
				//index 1 buy orders
				
				profitISK = curentItemData[1].subtract(curentItemData[0]);
				trade.setProfitInISK(profitISK);
				
				profitPercent = profitISK.divide(curentItemData[0],4,BigDecimal.ROUND_HALF_UP);
				trade.setProfitPercentage(profitPercent);
				
				//if the percentage is in the acceptable range add the item ID to the list
				if (profitPercent.compareTo(new BigDecimal(.15)) > 0 && profitPercent.compareTo(new BigDecimal(.30)) < 0) {
					possibleTrades.add(trade);
				}
				
			//logic exception as some items will lack either buy or sell orders. 
			} catch (ArithmeticException e) {
				System.out.println("Could not compare itemID: " + itemID);
			} catch (Exception e) {
				//TODO: add logging
			}
		}
		//returns all suggested items
		return possibleTrades.toArray(new Trade[possibleTrades.size()]);
	}
}
