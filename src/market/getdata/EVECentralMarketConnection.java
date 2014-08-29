package market.getdata;

import java.net.URL;
import java.sql.ResultSet;

import org.w3c.dom.Document;

public class EVECentralMarketConnection extends URL_MarketConnection {
	
	public Document getXMLDoc(){
		try {
			//TODO: overloads for this method to accept multiple arguments for the URL
			marketDataURL = new URL("http://api.eve-central.com/api/quicklook?typeid=34&regionlimit=10000002&usesystem=30000142");
			
			buildXMLDoc();
			return doc;
			
		} catch (Exception e) {
			//doing nothing with this currently
			return null; 
		}
	}
	
	public Document getXMLDoc(String arg) {
		try {
		marketDataURL = new URL("http://api.eve-central.com/api/quicklook?typeid=" + arg + "&regionlimit=10000002&usesystem=30000142");
		buildXMLDoc();
		return doc;
		
		} catch (Exception e) {
			//not really doing anything with this either
			return null; 
		}
	}

	public Document getXMLDoc(ResultSet resultSet) {
		// TODO Auto-generated method stub
		try {
			StringBuilder urlBuilder = new StringBuilder();
			urlBuilder.append("http://api.eve-central.com/api/quicklook?");
			
						//marketDataURL = new URL("http://api.eve-central.com/api/quicklook?typeid=" +  + "regionlimit=10000002&usesystem=30000142");
			buildXMLDoc();
			return doc;		
		} catch (Exception e) {
			//not really doing anything with this either
			return null; 
		}
		
	}
}
