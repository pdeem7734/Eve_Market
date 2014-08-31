package market.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import market.autotrader.*;
import market.putdata.*;


//completely lacks a console UI at this point just an entry point to the application

public class ConsoleUI {
	
	//entry point for the application
	//we will need to redefine to allow basic trading suggestings once trader/default trader have been defined
	public static void main(String args[]){
		getDefaultTrades();
	}
	
	private static void startCRESTTransfer() {
		CRESTTransfer ct = new CRESTTransfer();
		ct.getAndTransfer();
	}
	private static void startCRESTTransfer(String[] itemIDs) {
		CRESTTransfer ct = new CRESTTransfer();
		ct.getAndTransfer(itemIDs);
	}
	
	private static void startEVECentralTransfer() {
		EVECentralTransfer et = new EVECentralTransfer();
		et.getAndTransfer();
	}
	
	private static void startEVECentralTransfer(String[] itemIDs) {
		EVECentralTransfer et = new EVECentralTransfer();
		et.getAndTransfer(itemIDs);
	}
	
	
	private static void getDefaultTrades() {
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
