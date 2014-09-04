package market.putdata;

import java.io.IOException;
import java.util.*;

import market.database.*;
import market.dataconnections.*;

import org.json.simple.*;

import java.sql.*;

//this class has gotten way out of hand...whoops
public class CRESTTransfer extends DataTransfer {
	
	MySQLMarketConnection sqlCon;
	ArrayList<String> itemIDList = new ArrayList<String>();
	HashSet<String> historicalJSON = new HashSet<String>();
	
	//itme IDs that have been places
	private String[] itemIDs = null;
	private int idNumber = 0;
	
	public CRESTTransfer(MySQLMarketConnection sqlCon) {
		this.sqlCon = sqlCon;
	}
	
	//the default transfer method gets all item ids from the sql table 
	//then calls the paramaterized method with the list of methods
	@Override
	public void getAndTransfer() {
		try {
			selectStatement = sqlCon.getMarketStatement();
			selectResultSet = selectStatement.executeQuery("SELECT itemID FROM itemtypes");
			
			//add all of the item ids to an araylist
			while (selectResultSet.next()){
				itemIDList.add(selectResultSet.getString("itemID"));
			}
			getAndTransfer(itemIDList.toArray(new String[itemIDList.size()]));
		} catch (Exception e) {
			//doing nothing with this
		}
		
	}
	
	//this methos is called by the nested class to get the next item ID that needs to be pulled from the crest API
	private synchronized String getNextID() {
		//if there are still item id's available reutnr to the string
		//else return null
		try {
			if (idNumber < itemIDs.length) {
				System.out.printf("Item ID number %d request of %d\n", idNumber, itemIDs.length);
				return itemIDs[idNumber++];
			} else return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	//this method is called by the nest class to updated what item ID's have been insterted into the SQL datatabase
	private synchronized void putItemID(String itemID) {
		historicalJSON.add(itemID);
	}

	//this method is used ot get and transferdata to the SQL satabased
	@Override
	public void getAndTransfer(String[] itemIDs) {
		
		this.itemIDs = itemIDs;		
		try {
			insertStatement = sqlCon.getMarketStatement();
			
			//creates 20 import threads to help accelerate the JSON get from the crest endpoint
			Thread[] importThreads = new Thread[20];
			
			for (int k = 0; k< importThreads.length; k++) {
				importThreads[k] = new importThread();
				importThreads[k].start();
				Thread.sleep(100);
			}
			
			
			
			for (Thread thread : importThreads) {
				thread.join();
			}
			importThreads = null;
			
			for (String itemID: this.itemIDs) {
				if (historicalJSON.contains(itemID)); 
				else System.out.printf("Key %s not loaded", itemID);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			//doing nothing with this currently
		}
	}
	
	
	
	//have to make this multi-threaded as it takes >1 second for each item to load
	private class importThread extends Thread {
		
		CRESTConnection urlMarketCon = new CRESTConnection();
		JSONArray temparray = null;
		Boolean notLoadedURL = true;
		
		//the insert query that will be used to put the data elements into the database
		String insertQuery = "INSERT INTO CRESTHistorical (itemID, Volume, orderCount, lowPrice, highPrice, AvgPrice, marketDate)" 
				+ "VALUES (%s, %s, %s, %s, %s, %s,'%s')";
		
		int attempts = 0;
		public void run() {
			Statement localInsert;
			
			//sets it's own connection to the SQL Server
			try {
				localInsert = new MySQLMarketConnection().getMarketStatement();
			} catch (Exception e) {
				localInsert = null;
			}
			
			//gets the itemID to iterate on
			String itemID = null; 
			while ((itemID = getNextID()) != null){
				attempts = 0;
				do {
					try {
						//gets the JSON array 
						temparray = urlMarketCon.getHistorical(itemID);
						
						//creating and instansiating the input here so values can't accidently carry over
						String[] input = new String[7];
						JSONObject singleDayItem;						
						
						for (Object dayJSON : temparray) {
							//cast to a JSON object so we can get the string value
							singleDayItem = (JSONObject) dayJSON;
							
							//copy the items into the input string so we can easily intup them to the inset query
							input[0] = itemID;
							input[1] = String.valueOf(singleDayItem.get("volume_str"));
							input[2] = String.valueOf(singleDayItem.get("orderCount"));
							input[3] = String.valueOf(singleDayItem.get("lowPrice"));
							input[4] = String.valueOf(singleDayItem.get("highPrice"));
							input[5] = String.valueOf(singleDayItem.get("avgPrice"));
							input[6] = String.valueOf(singleDayItem.get("date")).substring(0,10);
							
							//insert the items, allow the concurrent access featrues of MySQL to control thread saftey here
							//might be best at some point to create a blocking queue that another method can pull
							//to place insert the items rather then having each thread do it independatly
							localInsert.execute(String.format(insertQuery, input[0], input[1], 
									input[2], input[3], input[4], input[5], input[6]));
							
						}
						//if we have made it to this step without error all elements have been loaded correctly. 
						putItemID(itemID);
						notLoadedURL = false;
					} catch(IOException e) {
						e.printStackTrace();
						attempts ++;
						try {
							Thread.sleep(100);
						} catch (Exception p){
							
						}
					} catch (Exception e) {
						//doing nothing with this current;y
					}
				//the application will attempt to load the item 30 times or until it has been loaded.
				} while (notLoadedURL && attempts < 30);
			}
		}
	}
}
