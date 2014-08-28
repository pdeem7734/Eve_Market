package market.database;

import java.sql.*;
public class MySQLConnection {
	private Connection connection;
	private Statement statement;
	private PreparedStatement preparedStatement;
	private ResultSet resultSet;
		
	//mostly just testing this at this point.
	//tested a few things
	public void readDataBase() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost/feedback?user=general");
		statement = connection.createStatement();
		resultSet = statement.executeQuery("SELECT * FROM market_data.historicaldaily");
		
		System.out.println(resultSet.getString("id"));
		System.out.println(resultSet.getString("item_id"));
		System.out.println(resultSet.getString("sellmin"));
	}
	
	public static void main(String args[]){
		MySQLConnection con = new MySQLConnection();
		
		try {
			con.readDataBase();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
