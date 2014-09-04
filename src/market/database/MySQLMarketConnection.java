package market.database;

import java.sql.*;

public class MySQLMarketConnection implements SQL_Connection {
	private Connection connection;
	private Statement statement;
	private String connectionString;
	//Returns the market statement
	@Override
	public Statement getMarketStatement() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection(connectionString);
		statement = connection.createStatement();
		return statement;
	}
	//
	public MySQLMarketConnection() {
		try {
			connectionString = "jdbc:mysql://localhost:3306/market_data?user=general";
		} catch (Exception e) {
			//open connection failed
		}
	}
	public MySQLMarketConnection(String serverLocation, String userID, String password) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID + "&password=" + password;
		} catch (Exception e) {
			//open connection failed
		}
	}
	public MySQLMarketConnection(String serverLocation, String userID) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID;
		} catch (Exception e) {
			//open connection failed
		}
	}
	
	public MySQLMarketConnection(String serverLocation) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=general";
		} catch (Exception e) {
			//open connection failed
		}
	}
	
	//below methods act like constuctors and change the connection string 
	public void changeConnectionString(String serverLocation, String userID, String password) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID + "&password=" + password;
		} catch (Exception e) {
			//open connection failed
		}
	}
	public void changeConnectionString(String serverLocation, String userID) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID;
		} catch (Exception e) {
			//open connection failed
		}
	}
	
	public void changeConnectionString(String serverLocation) {
		try {
			connectionString = "jdbc:mysql://" + serverLocation
					+ "/market_data?user=general";
		} catch (Exception e) {
			//open connection failed
		}
	}
}
