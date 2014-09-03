package market.database;

import java.sql.*;

public class MySQLMarketConnection implements SQL_Connection {
	private Connection connection;
	private Statement statement;
		
	//Returns the market statement
	@Override
	public Statement getMarketStatement() throws SQLException, ClassNotFoundException {
		statement = connection.createStatement();
		return statement;
	}
	//
	public MySQLMarketConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/market_data?user=general");
		} catch (Exception e) {
			//open connection failed
		}
	}
	public MySQLMarketConnection(String serverLocation, String userID, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID + "&password=" + password);
		} catch (Exception e) {
			//open connection failed
		}
	}
	public MySQLMarketConnection(String serverLocation, String userID) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + serverLocation
					+ "/market_data?user=" + userID );
		} catch (Exception e) {
			//open connection failed
		}
	}
	
	public MySQLMarketConnection(String serverLocation) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + serverLocation
					+ "/market_data?user=general");
		} catch (Exception e) {
			//open connection failed
		}
	}
}
