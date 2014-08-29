package market.ui;

import java.math.*;
import java.util.*;
import java.io.*;
import java.sql.*;

import javax.xml.xpath.*;

import org.w3c.dom.*;

import market.database.MySQLMarketConnection;
import market.getdata.*;

//completely lacks a console UI at this point ended up repurposing to write data to sql table.
public class ConsoleUI {
	
	//entry point for the application
	public static void main(String args[]){
		//node list for buy and sell orders
		NodeList sellList;
		NodeList buyList;
		//array list for the same
		ArrayList <BigDecimal> sellArrayList = new ArrayList<BigDecimal>();
		ArrayList <BigDecimal> buyArrayList = new ArrayList<BigDecimal>();
		
		EVECentralMarketConnection marketCon = new EVECentralMarketConnection();
		MySQLMarketConnection myMarketData = new MySQLMarketConnection();
		Statement marketStatement;
		Statement marketStatement2;
		ResultSet resultSet;
		try {
			marketStatement = myMarketData.getMarketStatement();
			marketStatement2 = myMarketData.getMarketStatement();
			resultSet = marketStatement.executeQuery("SELECT itemID FROM market_data.ItemTypes");
			
			//input reader to get the itemID
			String itemID = "";
			
			while (resultSet.next()){
				itemID = resultSet.getString("itemID");
				Document xmlDoc = marketCon.getXMLDoc(itemID);
				//simple entry point for the application at this point				
				XPath xpath = XPathFactory.newInstance().newXPath();			
				
				try {
					//get nodes for the the sell/buy orders
					sellList = (NodeList) xpath.evaluate("//sell_orders//order/price", xmlDoc, XPathConstants.NODESET);
					buyList = (NodeList) xpath.evaluate("//buy_orders//order/price", xmlDoc, XPathConstants.NODESET);		
					
					//write the prices for each sell/buy order to the array list then sort it 
					for (int k = 0; k < sellList.getLength(); k++){
						Node n = sellList.item(k);
						sellArrayList.add(new BigDecimal(n.getFirstChild().getNodeValue()));
					}
				
					for (int k = 0; k < buyList.getLength(); k++){
						Node n = buyList.item(k);
						buyArrayList.add(new BigDecimal(n.getFirstChild().getNodeValue()));
					}
					Collections.sort(buyArrayList);
					Collections.sort(sellArrayList);
					
					String sellMin = String.valueOf(Collections.min(sellArrayList));
					String buyMax = String.valueOf(Collections.max(buyArrayList));			
					
					marketStatement2.execute(String.format("INSERT INTO HistoricalEVECentral (item_ID,sellmin,buymax) VALUES (%s,%s,%s)", itemID, sellMin, buyMax));					
					sellArrayList.removeAll(sellArrayList);
					buyArrayList.removeAll(buyArrayList);
					
				} catch (Exception e) {
					//do nothing
					System.out.println(e);
					e.printStackTrace();
					System.out.println("Unable to connect or print to database");
				}
			}
		} catch (Exception e) {
			//bad things happened
			System.out.println("Bad stuff");
			System.out.println(e);
		}
	}
}
