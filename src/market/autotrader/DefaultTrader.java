package market.autotrader;

import market.database.*;
import market.putdata.*; 

import java.math.*;
import java.util.*;


public class DefaultTrader extends Trader {
	public DefaultTrader() {
		sqlConnection = new MySQLMarketConnection();
		loadMaps();
	}
	
	//TODO: string[][] will be replaced with a trade class that will contain similar information
	@Override
	public String[][] suggestTrades() {
		String[][] finalTrades; 
		Integer[] potentialTrades = itemIDs.toArray(new Integer[itemIDs.size()]);
		
		//search and validate
		potentialTrades = searchOrders(potentialTrades);
		potentialTrades = validateByVolume(potentialTrades);
		
		//update the current prices for the items that have been selected and validated 
		EVECentralTransfer ecTransfer = new EVECentralTransfer();		
		ArrayList<String> tempList = new ArrayList<String>();
		for (Integer itemID : potentialTrades) {
			tempList.add(itemID.toString());
		}
		ecTransfer.getAndTransfer(tempList.toArray(new String[tempList.size()]));
		ecTransfer = null;
		
		//check to ensure the trades still match criteria 
		potentialTrades = searchOrders(potentialTrades);
		
		//return the itemID's for the trades we have pulled 
		finalTrades = new String[potentialTrades.length][];
		for (int i = 0; i < potentialTrades.length; i++) {
			finalTrades[i] = new String[] {potentialTrades[i].toString()};
		}
		return finalTrades;
	}
	
	//validates that the trades passed to it have sufficent volume and return to be worth investing in.
	private Integer[] validateByVolume(Integer[] itemIDs) {
		BigDecimal[] curentItemData;
		BigDecimal profitISK;
		BigDecimal volume;
		BigDecimal iskByVolume;
		
		//the set of trades that have been validated
		HashSet<Integer> validatedTrades = new HashSet<Integer>();
		
		//iterate though all passed item ID's
		for (Integer itemID : itemIDs) {
			curentItemData = metaData.get(itemID);
			
			//gets the profit in isk multiplied by 5% of the total market volume over 24h
			//5% might be a bit high for expected market impact but will adjust as needed
			profitISK = curentItemData[1].subtract(curentItemData[0]);
			volume = curentItemData[0].add(curentItemData[4]);
			volume = volume.multiply(new BigDecimal(.05));
			iskByVolume = volume.multiply(profitISK);
			
			//if the volume is over 200 items traded in the past 24h
			//and the isk by 5%volume is over 10m validate the trade 
			if (volume.compareTo(new BigDecimal(200)) > 0 && (iskByVolume.compareTo(new BigDecimal(10_000_000)) > 0)) {
				validatedTrades.add(itemID);
			}
		}
		//return all validated trades
		return validatedTrades.toArray(new Integer[validatedTrades.size()]);
	}
	
	//searches though the itemID's passed to it looking for recomended trades
	private Integer[] searchOrders(Integer[] itemIDs) {
		//bid decimal has to be used as data can range from single values to billions 
		BigDecimal[] curentItemData;
		BigDecimal profitPercent;
		BigDecimal profitISK;
		
		//this is the item ID for the possible trades 
		HashSet<Integer> possibleTrades = new HashSet<Integer>();
		
		//cycles though all item ID's passed to it 
		for (Integer itemID: itemIDs) {
			try {
				//looks for values where the price difference between buy and sell is over 15% and under 30%
				curentItemData = new BigDecimal[] {buyOrders.get(itemID),sellOrders.get(itemID)};
				
				//index 0 sell orders
				//index 1 buy orders
				
				//gets the profit per item at current order price
				profitISK = curentItemData[1].subtract(curentItemData[0]);
				
				//calulates the percentage off the buy price
				profitPercent = profitISK.divide(curentItemData[0],4,BigDecimal.ROUND_HALF_UP);
				
				//if the precentage is in the acceptable range add the item ID to the list
				if (profitPercent.compareTo(new BigDecimal(.15)) > 0 && profitPercent.compareTo(new BigDecimal(.30)) < 0) {
					possibleTrades.add(itemID);
				}
				
			//logic excpetion as some items will lack either buy or sell orders. 
			} catch (ArithmeticException e) {
				System.out.println("Could not compare itemID: " + itemID);
			}
		}
		//returns all suggested items
		return possibleTrades.toArray(new Integer[possibleTrades.size()]);
	}
}
