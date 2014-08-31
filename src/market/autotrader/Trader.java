package market.autotrader;

import java.math.*;
import java.util.*;
import java.sql.*;

import market.database.*;
public abstract class Trader {
	
	SQL_Connection sqlConnection;	
	Statement selectStatement;
	
	//maps with the key being the itemID
	HashMap<Integer, BigDecimal> buyOrders = new HashMap<Integer, BigDecimal>();
	HashMap<Integer, BigDecimal> sellOrders = new HashMap<Integer, BigDecimal>();
	HashMap<Integer, BigDecimal[]> metaData = new HashMap<Integer, BigDecimal[]>();
	HashSet<Integer> itemIDs = new HashSet<Integer> ();
	
		
	//will return the slected trades
	//first holding the trade
	//second holding the info on the trade
	//may change this to an obj and make trade it's own class
	public abstract String[][] suggestTrades();
	
	
	//this will reload the maps with new information from the database 
	public void loadMaps() {
		//ensure we can connect to the database
		try {
			selectStatement = sqlConnection.getMarketStatement();
		} catch (Exception e) {
			System.out.println("unable to connect to database");
			e.printStackTrace();
		}
		loadOrderMaps();
		loadMetaMap();
	}
	
	//loads the order from the MySQL server
	private void loadOrderMaps() {
		try { 
			//runs a select query on the most recent orders
			ResultSet selectResults = selectStatement.executeQuery("SELECT * FROM evecentral AS histEC INNER JOIN " 
					+ "itemtypes AS items "
					+ "ON histEC.item_id = itemid AND histEC.datepulled = " 
					+ "(SELECT MAX(datepulled) FROM evecentral where item_id = histEC.item_id) ORDER BY items.itemid;");
			
			//iterates though all and adds to map
			while (selectResults.next()){
				int itemID = Integer.parseInt(selectResults.getString("item_id"));
				buyOrders.put(itemID, new BigDecimal(selectResults.getString("BuyMax")));
				sellOrders.put(itemID, new BigDecimal(selectResults.getString("SellMin")));
				itemIDs.add(itemID);
			}
			selectResults = null;
			
		} catch (Exception e) {
			System.out.println("unable to load order maps");
			e.printStackTrace();
		}
	}
	
	//loads mets data
	private void loadMetaMap() {
		try {
			//runs select query that will only contain the most recent meta data
			ResultSet selectResults = selectStatement.executeQuery("SELECT * FROM metadata AS histMD INNER JOIN "
					+ "itemtypes AS items "
					+ "ON histMD.itemid = items.itemid AND histMD.datepulled = " 
					+ "(SELECT MAX(datepulled) FROM metadata where itemid = histMD.itemid) ORDER BY items.itemid;");
			
			//add all the meta data to the map
			while (selectResults.next()) {
				BigDecimal[] tempArray = new BigDecimal[8];
				tempArray[0] = new BigDecimal(selectResults.getString("BuyVolume"));
				tempArray[1] = new BigDecimal(selectResults.getString("BuyAvg"));
				tempArray[2] = new BigDecimal(selectResults.getString("BuyMax"));
				tempArray[3] = new BigDecimal(selectResults.getString("BuyMin"));
				tempArray[4] = new BigDecimal(selectResults.getString("SellVolume"));
				tempArray[5] = new BigDecimal(selectResults.getString("SellAvg"));
				tempArray[6] = new BigDecimal(selectResults.getString("SellMax"));
				tempArray[7] = new BigDecimal(selectResults.getString("SellMin"));
				
				int itemID = Integer.parseInt(selectResults.getString("itemID"));
				
				
				metaData.put(itemID, tempArray);
			}
		} catch (Exception e) {
			System.out.println("unable to load metadata map");
			e.printStackTrace();
		}
	}
}
