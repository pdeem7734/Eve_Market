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
	
	@Override
	public String[][] suggestTrades() {
		String[][] finalTrades; 
		// TODO Auto-generated method stub
		Integer[] potentialTrades = itemIDs.toArray(new Integer[itemIDs.size()]);
		
		//search and validate
		potentialTrades = searchOrders(potentialTrades);
		potentialTrades = validateByVolume(potentialTrades);
		
		//update the makrte orders for the items we have selected
		EVECentralTransfer ecTransfer = new EVECentralTransfer();		
		ArrayList<String> tempList = new ArrayList<String>();
		for (Integer itemID : potentialTrades) {
			tempList.add(itemID.toString());
		}		
		ecTransfer.getAndTransfer(tempList.toArray(new String[tempList.size()]));
		
		//double check they still meet the criteria after pulling most recent available data
		potentialTrades = searchOrders(potentialTrades);
		
		finalTrades = new String[potentialTrades.length][];
		for (int i = 0; i < potentialTrades.length; i++) {
			finalTrades[i] = new String[] {potentialTrades[i].toString()};
		}
		return finalTrades;
	}
	
	private Integer[] validateByVolume(Integer[] itemIDs) {
		BigDecimal[] curentItemData;
		BigDecimal profitISK;
		BigDecimal volume;
		BigDecimal iskByVolume;
		
		HashSet<Integer> validatedTrades = new HashSet<Integer>();
		
		for (Integer itemID : itemIDs) {
			curentItemData = metaData.get(itemID);
			profitISK = curentItemData[1].subtract(curentItemData[0]);
			volume = curentItemData[0].add(curentItemData[4]);
			volume = volume.multiply(new BigDecimal(.05));
			
			iskByVolume = volume.multiply(profitISK);
			if (volume.compareTo(new BigDecimal(200)) > 0 && (iskByVolume.compareTo(new BigDecimal(10_000_000)) > 0)) {
				validatedTrades.add(itemID);
			}
			
		}
		return validatedTrades.toArray(new Integer[validatedTrades.size()]);
	}
	private Integer[] searchOrders(Integer[] itemIDs) {
		BigDecimal[] curentItemData;
		BigDecimal profitPercent;
		BigDecimal profitISK;
		
		HashSet<Integer> possibleTrades = new HashSet<Integer>();
		
		for (Integer itemID: itemIDs) {
			try {
				//looks for values where the price difference between buy and sell is over 15% and under 50%
				curentItemData = new BigDecimal[] {buyOrders.get(itemID),sellOrders.get(itemID)};
				
				profitISK = curentItemData[1].subtract(curentItemData[0]);
				profitPercent = profitISK.divide(curentItemData[0],4,BigDecimal.ROUND_HALF_UP);
				
				if (profitPercent.compareTo(new BigDecimal(.15)) > 0 && profitPercent.compareTo(new BigDecimal(.30)) < 0) {
					possibleTrades.add(itemID);
				}
			} catch (ArithmeticException e) {
				System.out.println("Could not compare itemID: " + itemID);
			}
		}
		return possibleTrades.toArray(new Integer[possibleTrades.size()]);
	}
}
