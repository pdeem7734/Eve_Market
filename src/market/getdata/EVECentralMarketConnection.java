package market.getdata;

import java.net.URL;
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
}
