package market.dataconnections;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class CRESTConnection extends URL_MarketConnection {
	//really stupid string but what ever is a test
	public synchronized JSONArray getHistorical(String itemID) throws IOException{
		JSONParser parser = new JSONParser();
		JSONArray marketData = null; 
		try {
			marketDataURL = new URL("http://public-crest.eveonline.com/market/10000002/types/" + itemID + "/history/");
			
			URLConnection urlConnection = marketDataURL.openConnection();			
			JSONObject response = (JSONObject) parser.parse(new InputStreamReader(urlConnection.getInputStream()));
			
			marketData = (JSONArray) response.get("items");
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Unable to load file");
		}
		return marketData;
	}
}
