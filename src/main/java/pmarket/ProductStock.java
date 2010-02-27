package pmarket;
import java.io.Serializable;

import currencies.Money;

/**
 * 
 */

/**
 * @author m
 *
 */
public class ProductStock implements Serializable {

	private static final long serialVersionUID = -8230409491904222791L;
	private int size;
	private String name;
	private String marketSectorName;
	private Money price;
	private Money marketValue;
	private Money fixedCost;
	private Money variableCost;
	public static final String currency = "EUR";
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setPrice(Money price) {
		this.price = price;
	}
	public Money getPrice() {
		if(price ==null) return new Money(currency,0.0);
		return price;
	}
	public void setMarketValue(Money value) {
		this.marketValue = value;
	}
	public Money getMarketValue() {
		if(marketValue ==null) return new Money(currency,0.0);
		return marketValue;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getSize() {
		return size;
	}
	/**
	 * Goig to be the property of RelationshipType.OWNED. If it eg "Gold", it is going to be offered only to customers 
	 * whose property of RelationshipType.IS_CUSTOMER is "Gold":
	 * @param marketSectorName
	 */
	public void setMarketSectorName(String marketSectorName) {
		this.marketSectorName = marketSectorName;
	}
	/**
	 * Goig to be the property of RelationshipType.OWNED. If it eg "Gold", it is going to be offered only to customers 
	 * whose property of RelationshipType.IS_CUSTOMER is "Gold":
	 * 
	 */
	public String getMarketSectorName() {
		return marketSectorName;
	}
	public void setFixedCost(Money fixCost) {
		this.fixedCost = fixCost;
	}
	public Money getFixedCost() {
		if(fixedCost ==null ) {
			return new Money(currency,0.0);
		}
		return fixedCost;
	}
	public void setVariableCost(Money varCost) {
		this.variableCost = varCost;
	}
	public Money getVariableCost() {
		if(variableCost ==null) return new Money(currency,0.0);
		return variableCost;
	}
	
	
	
}
