package market.ui;

import market.autotrader.*;
import market.putdata.*;


//completely lacks a console UI at this point just an entry point to the application

public class ConsoleUI {
	
	//entry point for the application
	//we will need to redefine to allow basic trading suggestings once trader/default trader have been defined
	public static void main(String args[]){
		startCRESTTransfer();
	}
	
	private static void startCRESTTransfer() {
		CRESTTransfer ct = new CRESTTransfer();
		ct.getAndTransfer();
	}
	
	private static void getTrades() {
		Trader newTrader = new DefaultTrader();
		String[][] suggestedTrades = newTrader.suggestTrades();
		
		for (String[] trade : suggestedTrades) {
			for (String s: trade) {
				System.out.print(s + ",");
			}
			System.out.print("\n");
		}
	}
}
