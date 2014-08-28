package market.ui;

import java.math.*;
import java.util.*;

import javax.xml.xpath.*;

import org.w3c.dom.*;

import market.getdata.*;


public class ConsoleUI {
	public static void main(String args[]){
		NodeList sellList;
		NodeList buyList;
		ArrayList <BigDecimal> sellArrayList = new ArrayList<BigDecimal>();
		ArrayList <BigDecimal> buyArrayList = new ArrayList<BigDecimal>();
		
		//simple entry point for the application at this point
		EVECentralMarketConnection marketCon = new EVECentralMarketConnection();		
		Document xmlDoc = marketCon.getXMLDoc();
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
			
		} catch (Exception e) {
			//doing nothing with this currently
		}
	
		for (BigDecimal sell: sellArrayList) {
			System.out.println("Sell: " + sell);
		}
		
		for (BigDecimal buy: buyArrayList) {
			System.out.println("Buy: " + buy);
		} 
		
		BigDecimal profitAmount;
		profitAmount = sellArrayList.get(0).subtract(buyArrayList.get(buyArrayList.size()- 1));
		System.out.println("Potential Profit: " + profitAmount);
		
	}
}
