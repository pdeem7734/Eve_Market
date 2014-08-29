package market.dataconnections;

import java.net.URL;
import java.sql.ResultSet;

import org.w3c.dom.Document;

public class EVECentralMarketConnection extends URL_MarketConnection {
		
	public Document getXMLOrders(String typeID) {
		try {
		marketDataURL = new URL("http://api.eve-central.com/api/quicklook?typeid=" + typeID + "&regionlimit=10000002&usesystem=30000142");
		buildXMLDoc();
		return doc;
		
		} catch (Exception e) {
			//not really doing anything with this either
			return null; 
		}
	}

	public Document getXMLMetaData(ResultSet resultSet) {
		// TODO Auto-generated method stub
		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append("http://api.eve-central.com/api/marketstat?");
			
			while (resultSet.next()) {
				urlBuilder.append("typeid=");
				urlBuilder.append(resultSet.getString("itemID"));
				urlBuilder.append("&");
			}
			
			marketDataURL = new URL(urlBuilder.toString());
			buildXMLDoc();
			return doc;		
		} catch (Exception e) {
			//not really doing anything with this either
			return null; 
		}
		
	}
}
