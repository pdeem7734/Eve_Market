package market.getdata;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;


public abstract class URL_MarketConnection {
	URL marketDataURL;
	Document doc; 
	
	protected void buildXMLDoc() {
		//this method will get the XML doc off of a url that has already been generated
		try {
			URLConnection dataConnection = marketDataURL.openConnection();
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = parser.parse(dataConnection.getInputStream());
		} catch (Exception e) {
			//doing nothing with this currently
		}
	}
	
	public abstract Document getXMLDoc();
}
