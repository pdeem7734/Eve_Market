package market.database;

import java.sql.*;

public class MySQLMarketConnection implements SQL_Connection {
	private Connection connection;
	private Statement statement;
		
	//reutrns the market statement
	@Override
	public Statement getMarketStatement() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/market_data?user=general");
		statement = connection.createStatement();
		return statement;
	}
}
