package market.putdata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.w3c.dom.NodeList;

import market.dataconnections.*;
import market.database.*;

public abstract class DataTransfer {
	//this class is an abstraction of a class that will take information from one of the classed in market.dataconnections
	//and transfer it to one of the classes in market.database
	
	//node/araay list for buy and sell orders
	NodeList sellList;
	NodeList buyList;
	
	ArrayList <BigDecimal> sellArrayList = new ArrayList<BigDecimal>();
	ArrayList <BigDecimal> buyArrayList = new ArrayList<BigDecimal>();	
	
	//statements to comuniate with the database
	Statement selectStatement;
	Statement insertStatement;
	ResultSet selectResultSet;
	
	//this method would get all the market items and update their respective elements
	public abstract void getAndTransfer();
	
	//this will get and transfer only the listed itemID's
	public abstract void getAndTransfer(String[] itemIDs);
	
	public abstract void getAndTranferMetaData();	
	public abstract void getAndTransferOrders();
	
	public abstract void getAndTranferMetaData(String[] itemIDs);	
	public abstract void getAndTransferOrders(String[] itemIDs);				

}
