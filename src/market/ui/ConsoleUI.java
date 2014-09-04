package market.ui;

import java.io.*;

import market.autotrader.*;
import market.putdata.*;
import market.database.*;


//basic console entry point for the application at this point
public class ConsoleUI {
	BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
	String input;
	String[] inputArray;
	Trader trader;
	SQL_Connection sqlConnection;
	
	
	//entry point for the application
	public static void main(String args[]){
		ConsoleUI mainUI = new ConsoleUI();
		mainUI.startConsoleInput();
	}
	
	public ConsoleUI() {
		sqlConnection = new MySQLMarketConnection();
		trader = new DefaultTrader(sqlConnection);
	}
	
	//starts the console input portion
	public void startConsoleInput() {
		System.out.println("Welcome to the console Trader");
		System.out.println("Default Trader has been loaded\n'-t getTrades' will print a list of suggested trades");
		System.out.println("'-h' will get a full list of commands'");
		MAIN: while (true) {
			System.out.print(">");
			try {
				
				inputArray = inputReader.readLine().split(" ");
				
				switch (inputArray[0]) {
				case "-h":
					printHelp();
					break;
				case "-t":
					trades(inputArray[1]);
					break;
				case "-ss":
					setServer(inputArray);
					break;
				case "-tc":
					testConnection();
					break;
				case "-exit": 
					System.out.println("goodby");
					break MAIN;
				default:
					System.out.println("Unrecoginized command");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Invalid Input");
			}
		}
	}

	private boolean testConnection() {
		if (sqlConnection.testConnection()) {
			System.out.println("SQL Connection Successful");
			return true;
		} else {
			System.out.println("SQL Connection Failed");
			return false;
		}
	}
	
	private void setServer(String[] serverArgs) {
		switch (serverArgs.length) {
		case 2:
			sqlConnection.changeConnectionString(serverArgs[1]);
			break;
		case 3:
			sqlConnection.changeConnectionString(serverArgs[1], serverArgs[2]);
			break;
		case 4:
			sqlConnection.changeConnectionString(serverArgs[1], serverArgs[2], serverArgs[3]);
			break;
		}
	}
	
	private void printHelp() {
		System.out.println("This is where we would help you, maybe");
	}
	
	private void trades(String operation) {
		switch (operation) {
		case "getTrades":
			if (testConnection()) {
				Trade[] suggestedTrades = trader.suggestTrades();
				for (Trade trade : suggestedTrades) {
					System.out.print(trade.getItemName() + ":" +trade.getItemID() + "\n");
				}
			}
			break;
		default : 
			System.out.println("Invalid Argument");
		}
	}
	
	
	
	//below are slightly outdated methods, but could prove useful for testing at some point
	private void startCRESTTransfer() {
		CRESTTransfer ct = new CRESTTransfer();
		ct.getAndTransfer();
	}
	private void startCRESTTransfer(String[] itemIDs) {
		CRESTTransfer ct = new CRESTTransfer();
		ct.getAndTransfer(itemIDs);
	}
	
	private void startEVECentralTransfer() {
		EVECentralTransfer et = new EVECentralTransfer();
		et.getAndTransfer();
	}
	
	private void startEVECentralTransfer(String[] itemIDs) {
		EVECentralTransfer et = new EVECentralTransfer();
		et.getAndTransfer(itemIDs);
	}
}
