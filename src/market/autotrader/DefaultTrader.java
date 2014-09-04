package market.autotrader;

import market.database.*;

import java.math.*;
import java.util.*;


public class DefaultTrader extends Trader {
	public DefaultTrader(SQL_Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
	}
	
	//TODO: string[][] will be replaced with a trade class that will contain similar information
	@Override
	public String[][] suggestTrades() {
		try {
			selectStatement = sqlConnection.getMarketStatement();
			loadItemIDs();
		} catch (Exception e) {
			System.out.println("unable to connect to database");
			e.printStackTrace();
		}
		
		String[][] finalTrades; 
		Integer[] potentialTrades = itemIDs.keySet().toArray(new Integer[itemIDs.size()]);
		
		//search and validate
		loadOrderMap(potentialTrades);
		potentialTrades = searchOrders(potentialTrades);
		loadMetaMap(potentialTrades);
		potentialTrades = validateByVolume(potentialTrades);
		
		
		//validate trades against historical trending 
		loadCrestInfo(10, potentialTrades);
		potentialTrades = validateByTrend(potentialTrades);
		
		//potentialTrades = validateByTrend(potentialTrades);
		finalTrades = new String[potentialTrades.length][];
		
		//returns a string array for each trade. 
		for (int i = 0; i < potentialTrades.length; i++) {
			finalTrades[i] = new String[] {itemIDs.get(Integer.parseInt(potentialTrades[i].toString())), potentialTrades[i].toString()};
		}
		return finalTrades;
	}
	
	
	//this method will check the items listed and ensure there is not a significant downward trend contained in the data loaded
	private Integer[] validateByTrend(Integer[] itemIDs) {
		ArrayList<Integer> validatedTrades = new ArrayList<Integer>();
		
		for (Integer itemID: itemIDs){ 
			try {
				ArrayList<BigDecimal> priceDifferences = new ArrayList<BigDecimal>();
				//calculates the average average across the days in question.
				//as well as the difference between each days average and the current days average
				//zeroth index will be the most recent day
				BigDecimal average = new BigDecimal(0);
				BigDecimal mostRecentDayAverage = crestData.get(itemID).lastEntry().getValue()[4];
				//this will need to be reworked
				for (BigDecimal[] big : crestData.get(itemID).values()) {
					average = average.add(big[0]);
					
					//most recent average - this average 
					priceDifferences.add(mostRecentDayAverage.subtract(big[0]));
				}
				average = average.divide(new BigDecimal(crestData.get(itemID).values().size()), 4);
				
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
				if (profitPercent.compareTo(new BigDecimal(.15)) > 0 && profitPercent.compareTo(new BigDecimal(.30)) < 0) {
					possibleTrades.add(itemID);
				}
				
			//logic exception as some items will lack either buy or sell orders. 
			} catch (ArithmeticException e) {
				System.out.println("Could not compare itemID: " + itemID);
			} catch (Exception e) {
				System.out.println("No Market Information found for: " + super.itemIDs.get(itemID));
			}
		}
		//returns all suggested items
		return possibleTrades.toArray(new Integer[possibleTrades.size()]);
	}
}
