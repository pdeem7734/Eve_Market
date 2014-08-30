package market.putdata;

import java.util.ArrayList;
import java.util.Collections;
import java.math.*;

import javax.xml.xpath.*;

import org.w3c.dom.*;

import market.dataconnections.*;
import market.database.*;

public class EVECentralTransfer extends DataTransfer {
	EVECentralMarketConnection urlMarketCon = new EVECentralMarketConnection();
	MySQLMarketConnection sqlCon = new MySQLMarketConnection();
	
	ArrayList<String> itemIDList = new ArrayList<String>();
	ArrayList<Document> metaDataXML = new ArrayList<Document>();
	
	@Override
	public void getAndTransfer() {
		try {
			//first we have to get all of the item ID's to import
			selectStatement = sqlCon.getMarketStatement();
			selectResultSet = selectStatement.executeQuery("SELECT itemID FROM itemtypes");
			
			//add all of the item ids to an araylist
			while (selectResultSet.next()){
				itemIDList.add(selectResultSet.getString("itemID"));
			}
			selectStatement = null;
			selectResultSet = null;
			
			//transfer meta data first			
			getAndTranferMetaData(itemIDList.toArray(new String[itemIDList.size()]));	
			getAndTransferOrders(itemIDList.toArray(new String[itemIDList.size()]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getAndTransfer(String[] itemIDs) {
		getAndTranferMetaData(itemIDs);
		getAndTransferOrders(itemIDs);		
	}
	
	@Override
	public void getAndTranferMetaData(){
		try {
			//first we have to get all of the item ID's to import
			selectStatement = sqlCon.getMarketStatement();
			selectResultSet = selectStatement.executeQuery("SELECT itemID FROM itemtypes");
			
			//add all of the item ids to an araylist
			while (selectResultSet.next()){
				itemIDList.add(selectResultSet.getString("itemID"));
			}
			selectStatement = null;
			selectResultSet = null;
			
			//transfer meta data first			
			getAndTranferMetaData(itemIDList.toArray(new String[itemIDList.size()]));
			
			
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getAndTransferOrders(){
		try {
			//first we have to get all of the item ID's to import
			selectStatement = sqlCon.getMarketStatement();
			selectResultSet = selectStatement.executeQuery("SELECT itemID FROM itemtypes");
			
			//add all of the item ids to an araylist
			while (selectResultSet.next()){
				itemIDList.add(selectResultSet.getString("itemID"));
			}
			selectStatement = null;
			selectResultSet = null;
			
			//transfer meta data first			
			getAndTransferOrders(itemIDList.toArray(new String[itemIDList.size()]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void getAndTranferMetaData(String[] itemIDs){
		//insert the meta data first
		XPath xpath = XPathFactory.newInstance().newXPath();
		int k = 1;
		
		//TODO: optimize the market pull as this can take 100 elements at a time
		//will probably make it threaded
		for (String item : itemIDs) {
			metaDataXML.add(urlMarketCon.getXMLMetaData(new String[] {item}));
			System.out.printf("Imported Meta item %d of %d", k++, itemIDs.length);
		}
		
		try {
			insertStatement = sqlCon.getMarketStatement();
			//array elements 0 = buy 1 = sell
			String[] volume = new String[2];
			String[] avg = new String[2];
			String[] max = new String[2];
			String[] min = new String[2];
			
			NodeList buyTemp;
			NodeList sellTemp;
			
			String query = "INSERT INTO historicalmetadata (itemid, buyvolume, buyavg, buymax, buymin, sellvolume," +
					" sellavg, sellmax, sellmin)VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)";
			for (Document doc : metaDataXML) {
				try {
				buyTemp = (NodeList) xpath.evaluate("//buy/volume", doc, XPathConstants.NODESET);
				sellTemp = (NodeList) xpath.evaluate("//sell/volume", doc, XPathConstants.NODESET);
				volume[0] = buyTemp.item(0).getFirstChild().getNodeValue();
				volume[1] = sellTemp.item(0).getFirstChild().getNodeValue();					
				buyTemp = null;
				sellTemp = null;
				
				buyTemp = (NodeList) xpath.evaluate("//buy/avg", doc, XPathConstants.NODESET);
				sellTemp = (NodeList) xpath.evaluate("//sell/avg", doc, XPathConstants.NODESET);
				avg[0] = buyTemp.item(0).getFirstChild().getNodeValue();
				avg[1] = sellTemp.item(0).getFirstChild().getNodeValue();					
				buyTemp = null;
				sellTemp = null;
				
				buyTemp = (NodeList) xpath.evaluate("//buy/max", doc, XPathConstants.NODESET);
				sellTemp = (NodeList) xpath.evaluate("//sell/max", doc, XPathConstants.NODESET);
				max[0] = buyTemp.item(0).getFirstChild().getNodeValue();
				max[1] = sellTemp.item(0).getFirstChild().getNodeValue();					
				buyTemp = null;
				sellTemp = null;
				
				buyTemp = (NodeList) xpath.evaluate("//buy/min", doc, XPathConstants.NODESET);
				sellTemp = (NodeList) xpath.evaluate("//sell/min", doc, XPathConstants.NODESET);
				min[0] = buyTemp.item(0).getFirstChild().getNodeValue();
				min[1] = sellTemp.item(0).getFirstChild().getNodeValue();					
				buyTemp = null;
				sellTemp = null;
				
				String itemID = (String) xpath.evaluate("//type/@id", doc, XPathConstants.STRING);

				
				insertStatement.execute(String.format(query, itemID, volume[0], avg[0], max[0], min[0], volume[1], 
						avg[1], max[1], min[1]));	
				} catch (Exception e) {
					System.out.println("failed once");
					buyTemp = null;
					sellTemp = null;
				}
			} 
			metaDataXML.removeAll(metaDataXML);
		}catch (Exception e) {
			System.out.println("Meta Data to SQL Failed");
			e.printStackTrace();
		}
	}
	
	@Override
	//TODO: Finish this method
	public void getAndTransferOrders(String[] itemIDs){
		
		ArrayList<Document> orderXMLs = new ArrayList<Document>();
		int l = 1;
		for (String item : itemIDs) {
			orderXMLs.add(urlMarketCon.getXMLOrders(item));
			System.out.printf("Imported item %d of %d\n", l++, itemIDs.length);
		}
		
		for (Document xmlDoc : orderXMLs) {
			//simple entry point for the application at this point
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			try {
				insertStatement = sqlCon.getMarketStatement();
				//get nodes for the the sell/buy orders
				sellList = (NodeList) xpath.evaluate("//sell_orders//order/price", xmlDoc, XPathConstants.NODESET);
				buyList = (NodeList) xpath.evaluate("//buy_orders//order/price", xmlDoc, XPathConstants.NODESET);		
				String itemID = (String) xpath.evaluate("//item", xmlDoc, XPathConstants.STRING);
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
				
				insertStatement.execute(String.format("INSERT INTO HistoricalEVECentral (item_ID,sellmin,buymax) VALUES (%s,%s,%s)", 
						itemID, sellMin, buyMax));					
				sellArrayList.removeAll(sellArrayList);
				buyArrayList.removeAll(buyArrayList);
				
			} catch (Exception e) {
				//do nothing
				e.printStackTrace();
				System.out.println("Unable to connect or print to database");
			}
		}		
	}
}
