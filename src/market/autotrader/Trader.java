package market.autotrader;


//this may need to be changed to an interface as most of it's implementation got moved to it's own class
public abstract class Trader {
	MarketData marketData;
	public abstract Trade[] suggestTrades();		
}
