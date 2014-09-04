package market.autotrader;

import java.math.*;
//this class will contain the various information for the specific trade
public class Trade {
	Integer itemID;
	String itemName;
	BigDecimal profitInISK = new BigDecimal(0);
	BigDecimal profitPercentage = new BigDecimal(0);
	BigDecimal profitByVolume = new BigDecimal(0);
	BigDecimal volume = new BigDecimal(0);
	
	public Trade(Integer itemID, String itemName) {
		this.itemID = itemID;
		this.itemName = itemName;
	}
	
	//set methods for the various fields
	public void setProfitInISK(BigDecimal profit) {
		this.profitInISK = profit;
		calcProfitByVolume();
	}
	
	public void setProfitPercentage(BigDecimal profit){
		this.profitPercentage = profit;
	}
	
	public void setVolume(BigDecimal volume) {
		this.volume = volume;
		calcProfitByVolume();
	}
	
	private void calcProfitByVolume() {
		profitByVolume = volume.multiply(profitInISK);
	}
	
	//get methods for the various fields
	//don't want to make them public because implementation of this class may change
	public Integer getItemID() {
		return itemID;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public BigDecimal getProfitInISK() {
		return profitInISK;
	}
	
	public BigDecimal getProfitPercentage(){
		return profitPercentage;
	}
	
	public BigDecimal getVolume() {
		return volume;
	}
	
	public BigDecimal getProfitByVolume() {
		return profitByVolume;
	}
}
