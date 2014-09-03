package market.autotrader;

import java.math.*;
import java.util.*;
import java.sql.*;

import market.database.*;

//I am really starting to think that most of this should have been it's own class
//will need to refactor this at some point as this class is starting to do too much
public abstract class Trader {
	
	SQL_Connection sqlConnection;	
	Statement selectStatement;
	
	//maps with the key being the itemID
	HashMap<Integer, BigDecimal> buyOrders = new HashMap<Integer, BigDecimal>();
	HashMap<Integer, BigDecimal> sellOrders = new HashMap<Integer, BigDecimal>();
	HashMap<Integer, BigDecimal[]> metaData = new HashMap<Integer, BigDecimal[]>();
	HashMap<Integer, String> itemIDs = new HashMap<Integer, String> ();
	HashMap<Integer, TreeMap<String, BigDecimal[]>> crestData = new HashMap<Integer, TreeMap<String, BigDecimal[]>>();
	//first dimension will be the distance from current date
	//second will be the specific trend information for that date
		
	//will return the slected trades
	//first holding the trade
	//second holding the info on the trade
	//may change this to an obj and make trade it's own class
	public abstract String[][] suggestTrades();
	
	
	//this will reload the maps with new information from the database 
	public void loadMaps() {
		//ensure we can connect to the database
	}
	
	//this will load all of our itemID's to the class
	protected void loadItemIDs() {
		try {
			ResultSet selectResults = selectStatement.executeQuery("SELECT * FROM itemtypes");
			
			while (selectResults.next()) {
				itemIDs.put(Integer.parseInt(selectResults.getString("itemID")), selectResults.getString("itemName"));
			}
		} catch (Exception e) {
			//add logging
		}
		
	}
	
	//loads the order from the MySQL server
	protected void loadOrderMap(Integer[] itemIDs) {
		//stupid select string will need to make smaller
		String selectString = "SELECT * FROM evecentral AS histEC INNER JOIN " 
				+ "itemtypes AS items "
				+ "ON histEC.item_id = itemid AND histEC.datepulled = " 
				+ "(SELECT MAX(datepulled) FROM evecentral WHERE item_id = histEC.item_id) "
				+ "WHERE itemID = %d ORDER BY items.itemid;";
		try { 
			//runs a select query on the most recent orders
			for (Integer itemID : itemIDs) {
				ResultSet selectResults = selectStatement.executeQuery(String.format(selectString, itemID));
				
				//iterates though all and adds to map
				if  (selectResults.next()){
					buyOrders.put(itemID, new BigDecimal(selectResults.getString("BuyMax")));
					sellOrders.put(itemID, new BigDecimal(selectResults.getString("SellMin")));
				}
				selectResults = null;
			}
		} catch (Exception e) {
			System.out.println("unable to load order maps");
			e.printStackTrace();
		}
	}
	
	//this will load any requisite data from CREST
	protected void loadCrestInfo(int range, Integer[] itemIDs) {
		String selectQuery = "SELECT DISTINCT "
				+ "itemID, Volume, ordercount, lowprice, highprice, avgprice, marketdate FROM CRESTHistorical "
				+ "WHERE ItemID = %d ORDER BY MarketDate DESC LIMIT %d";
		
		for (Integer itemID : itemIDs) {
			//this will be the step to import crest information
			try {	
				ResultSet selectResults = selectStatement.executeQuery(String.format(selectQuery, itemID, range));
				
				TreeMap<String, BigDecimal[]> individualCrestData = new TreeMap<String, BigDecimal[]>();
				BigDecimal[] trendData = new BigDecimal[5];
				String rowDate;
				
				
				while (selectResults.next()) {
					rowDate = selectResults.getString("MarketDate");
					trendData[0] = new BigDecimal(selectResults.getString("Volume"));
					trendData[1] = new BigDecimal(selectResults.getString("orderCount"));
					trendData[2] = new BigDecimal(selectResults.getString("lowPrice"));
					trendData[3] = new BigDecimal(selectResults.getString("highPrice"));
					trendData[4] = new BigDecimal(selectResults.getString("avgPrice"));	
					individualCrestData.put(rowDate, trendData);					
					crestData.put(itemID, individualCrestData);
				}
				
			} catch (Exception e) {
				//add logging
			}
		}
	}
	
	//loads mets data
	protected void loadMetaMap(Integer[] itemIDs) {
		String selectString = "SELECT * FROM metadata AS histMD INNER JOIN "
				+ "itemtypes AS items "
				+ "ON histMD.itemid = items.itemid AND histMD.datepulled = " 
				+ "(SELECT MAX(datepulled) FROM metadata where itemid = histMD.itemid) ORDER BY items.itemid;";
		try {
			//runs select query that will only contain the most recent meta data
			for (Integer itemID : itemIDs){
				ResultSet selectResults = selectStatement.executeQuery(String.format(selectString, itemID));
				
				Statement selectCRESTStatement = sqlConnection.getMarketStatement();
				ResultSet selectCRESTResults = selectCRESTStatement.executeQuery(String.format("SELECT DISTINCT * FROM CRESTHistorical WHERE ItemID = %d ORDER BY MarketDate DESC LIMIT 1", itemID));

				if (selectResults.next()) {
					BigDecimal[] tempArray = new BigDecimal[9];
					tempArray[0] = new BigDecimal(selectResults.getString("BuyVolume"));
					tempArray[1] = new BigDecimal(selectResults.getString("BuyAvg"));
					tempArray[2] = new BigDecimal(selectResults.getString("BuyMax"));
					tempArray[3] = new BigDecimal(selectResults.getString("BuyMin"));
					tempArray[4] = new BigDecimal(selectResults.getString("SellVolume"));
					tempArray[5] = new BigDecimal(selectResults.getString("SellAvg"));
					tempArray[6] = new BigDecimal(selectResults.getString("SellMax"));
					tempArray[7] = new BigDecimal(selectResults.getString("SellMin"));	
					
					if (selectCRESTResults.next()) {
						tempArray[8] = new BigDecimal(selectCRESTResults.getString("volume"));
					} else {
						tempArray[8] = new BigDecimal("0");
					}
					
					metaData.put(itemID, tempArray);
				}
			}
		} catch (Exception e) {
			System.out.println("unable to load metadata map");
			e.printStackTrace();
		}
	}
}
