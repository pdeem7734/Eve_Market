package market.database;

import java.sql.SQLException;
import java.sql.Statement;

//will probably add more to this interface in time as I understand more of what i will need from the SQL server
public interface SQL_Connection {
	public Statement getMarketStatement() throws SQLException, ClassNotFoundException;
}
