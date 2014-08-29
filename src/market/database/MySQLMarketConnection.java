package market.database;

import java.sql.*;

public class MySQLMarketConnection {
	private Connection connection;
	private Statement statement;
	private ResultSet resultSet;
		
	//mostly just testing this at this point.
	//tested a few things
	public void readDataBase() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/market_data?user=general");
		statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM market_data.HistoricalEVECentral");
		
		while(resultSet.next()){
			System.out.println(resultSet.getString("id"));
			System.out.println(resultSet.getString("item_id"));
			System.out.println(resultSet.getString("sellmin"));
		}
	}
	
	public Statement getMarketStatement() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/market_data?user=general");
		statement = connection.createStatement();
		return statement;
	}
	
	
	public static void main(String args[]){
		MySQLMarketConnection con = new MySQLMarketConnection();
		
		try {
			con.readDataBase();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
