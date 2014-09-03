package market.autotrader;

import market.database.*;
import market.putdata.*; 

import java.math.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;


public class DefaultTrader extends Trader {
	public DefaultTrader() {
		sqlConnection = new MySQLMarketConnection();
		try {
			selectStatement = sqlConnection.getMarketStatement();
		} catch (Exception e) {
			System.out.println("unable to connect to database");
			e.printStackTrace();
		}
	}
	
	public void test(){
		loadItemIDs();
		loadCrestInfo(10, new Integer[] {34});
		System.out.println(crestData.get(Integer.parseInt("34")).lastKey());	
		loadOrderMap(new Integer[] {34});
		System.out.println(buyOrders.get(34));
		loadMetaMap(new Integer[] {34});
		System.out.println(metaData.get(34)[8]);
	}
	
	//TODO: string[][] will be replaced with a trade class that will contain similar information
	@Override
	public String[][] suggestTrades() {
		String[][] finalTrades; 
		Integer[] potentialTrades = itemIDs.keySet().toArray(new Integer[itemIDs.size()]);
		
		//search and validate
		potentialTrades = searchOrders(potentialTrades);
		potentialTrades = validateByVolume(potentialTrades);
		
		//check to ensure the trades still match criteria 
		potentialTrades = searchOrders(potentialTrades);
		
		//validate trades against historical trending 
		//potentialTrades = validateByTrend(potentialTrades);
		finalTrades = new String[potentialTrades.length][];
		
		//returns a string array for each trade. 
		for (int i = 0; i < potentialTrades.length; i++) {
			finalTrades[i] = new String[] {itemIDs.get(Integer.parseInt(potentialTrades[i].toString())), potentialTrades[i].toString()};
		}
		return finalTrades;
	}
	
	/*
	//this method will check the items listed and ensure there is not a signifignant downward trend in the last 10 days
	private Integer[] validateByTrend(Integer[] itemIDs) {		
		for (Integer itemID: itemIDs){ 
			try {		
				//calculates the average average across the days in question.
				//as well as the difference between each days average and the current days average
				//zeroth index will be the most recent day
				BigDecimal average = new BigDecimal(0);
				
				//this will need to be reworked
				for (BigDecimal[] big : crestData.get(itemID).values()) {
					average = average.add(big[0]);
					
					//most recent average - this average 
					priceDifferences.add(curentItemAverages.get(0).subtract(big));
				}
				average = average.divide(new BigDecimal(curentItemAverages.size()), 4);
				
				//calculate average change -1 because the price difference contained today - today
				BigDecimal averageChange = new BigDecimal(0);
				for (BigDecimal big: priceDifferences) {
					averageChange = averageChange.add(big);
				}
				averageChange = averageChange.divide(new BigDecimal(priceDifferences.size() - 1), 4);

				//item has been validated
				if (averageChange.compareTo(average.multiply(new BigDecimal(-.05))) > 0) {
					validatedTrades.add(itemID);
				}				
			} catch (Exception e) {
				//unable to validate item for one reason or another 
				e.printStackTrace();
				break;
			}
		}
		
		return validatedTrades.toArray(new Integer[validatedTrades.size()]);
	}
	
	*/
	
	//validates that the trades passed to it have sufficent volume and return to be worth investing in.
	private Integer[] validateByVolume(Integer[] itemIDs) {
		BigDecimal[] curentItemData;
		BigDecimal profitISK;
		BigDecimal volume;
		BigDecimal iskByVolume;		
		HashSet<Integer> validatedTrades = new HashSet<Integer>();

		//iterate though all passed item ID's
		for (Integer itemID : itemIDs) {
			curentItemData = metaData.get(itemID);
			try {

				//gets the profit in isk multiplied by 5% of the total market volume over 24h
				//5% might be a bit high for expected market impact but will adjust as needed
				profitISK = curentItemData[1].subtract(curentItemData[0]);
				volume = curentItemData[8];		
				iskByVolume = volume.multiply(profitISK).multiply(new BigDecimal(.05));
				
				//if the volume is over 200 items traded in the past 24h
				//and the isk by 5%volume is over 10m validate the trade				
				if (volume.compareTo(new BigDecimal("200")) > 0 && (iskByVolume.compareTo(new BigDecimal(1_000_000)) > 0)) {
					validatedTrades.add(itemID);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//return all validated trades
		return validatedTrades.toArray(new Integer[validatedTrades.size()]);
	}
	
	//searches though the itemID's passed to it looking for recommended trades
	private Integer[] searchOrders(Integer[] itemIDs) {
		BigDecimal[] curentItemData;
		BigDecimal profitPercent;
		BigDecimal profitISK;
		
		//this is the item ID for the possible trades 
		HashSet<Integer> possibleTrades = new HashSet<Integer>();
		
		//cycles though all item ID's passed to it 
		for (Integer itemID: itemIDs) {
			try {
				curentItemData = new BigDecimal[] {buyOrders.get(itemID),sellOrders.get(itemID)};
				
				//index 0 sell orders
				//index 1 buy orders
				
				profitISK = curentItemData[1].subtract(curentItemData[0]);
				profitPercent = profitISK.divide(curentItemData[0],4,BigDecimal.ROUND_HALF_UP);
				
				//if the percentage is in the acceptable range add the item ID to the list
				if (profitPercent.compareTo(new BigDecimal(.15)) > 0 && profitPercent.compareTo(new BigDecimal(.50)) < 0) {
					possibleTrades.add(itemID);
				}
				
			//logic exception as some items will lack either buy or sell orders. 
			} catch (ArithmeticException e) {
				System.out.println("Could not compare itemID: " + itemID);
			}
		}
		//returns all suggested items
		return possibleTrades.toArray(new Integer[possibleTrades.size()]);
	}
}
