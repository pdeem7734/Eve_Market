package market.ui;

import java.io.*;

import market.database.*;
import market.putdata.CRESTTransfer;
import market.putdata.EVECentralTransfer;
public class ServerConsole {
	BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
	String[] inputArray = new String[0];
	MySQLMarketConnection sqlConnection = new MySQLMarketConnection();
	public ServerConsole() {
	}
	public void startServerConsole() {
		System.out.println("This is the console access to the market server");
		MAIN: while (true) {
			try {
				System.out.print(">");
				inputArray = inputReader.readLine().split(" ");
				
				switch (inputArray[0]) {
				case "-ss":
					setServer(inputArray);
					break;
				case "-tc":
					testConnection();		
					break;
				case "-exit":
					System.out.println("goodby");
					break MAIN;
				case "-up":
					startCRESTTransfer();		
					startEVECentralTransfer();
					break;
				default:
					System.out.println("Unrecoginized command");
				}
			} catch (Exception e) {
				System.out.println("invalid input");
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
	
	private void startCRESTTransfer() {
		CRESTTransfer ct = new CRESTTransfer(sqlConnection);
		ct.getAndTransfer();
	}
	
	private void startEVECentralTransfer() {
		EVECentralTransfer et = new EVECentralTransfer(sqlConnection);
		et.getAndTransfer();
	}
	
	//entry point for the application
	public static void main(String[] args) {
		ServerConsole serverConsole = new ServerConsole();
		serverConsole.startServerConsole();
	}
}