
package pmarket;

import java.io.Serializable;

import org.strabil.currencies.Money;
import org.strabil.market.Agent;
import org.strabil.market.AgentSet;
import org.strabil.market.MarketSectorType;
import org.strabil.utils.DoTest;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import utils.Default;



@XStreamAlias("level")
public class LoyaltyLevel implements Serializable, AgentSet {

	private static final long serialVersionUID = 2095384450109433717L;

	@XStreamAlias("population")
	private int numberAgents;

	@XStreamAlias("minimum-budget")
	private Money[] minBudget = new Money[Agent.nBudgets];

	@XStreamAlias("maximum-budget")
	private Money[] maxBudget = new Money[Agent.nBudgets];

	@XStreamAlias("average-budget")
	private Money[] aveBudget = new Money[Agent.nBudgets];

	@XStreamAsAttribute
	@XStreamAlias("program-name")
	private String programName;

	@XStreamOmitField
	private MarketSectorType identifier;//ENUM

	private long period;

	public void setAveBudget(Money[] aveBudget) {
		this.aveBudget = aveBudget;
	}
	public Money[] getAveBudget() {
		return aveBudget;
	}

	public void setAveBudget(int i, Money aveBudget) {
		this.aveBudget[i] = aveBudget;
	}
	public Money getAveBudget(int i) {
		return aveBudget[i];
	}

	
	public void setPeriod(long period) {
		this.period = period;
	}
	public long getPeriod() {
		return period;
	}
	public void setMinBudget(int i, Money minBudget) {
		this.minBudget[i] = minBudget;
	}
	public Money getMinBudget(int i) {
		return minBudget[i];
	}
	public void setMaxBudget(int i, Money maxBudget) {
		this.maxBudget[i] = maxBudget;
	}
	public Money getMaxBudget(int i) {
		return maxBudget[i];
	}



	public void setMinBudget(Money[] minBudget) {
		this.minBudget = minBudget;
	}
	public Money[] getMinBudget() {
		return minBudget;
	}
	public void setMaxBudget(Money[] maxBudget) {
		this.maxBudget = maxBudget;
	}
	public Money[] getMaxBudget() {
		return maxBudget;
	}

	public void setNumberAgents(int numberAgents) {
		this.numberAgents = numberAgents;
	}
	public int getNumberAgents() {
		return numberAgents;
	}

	/**
	 * Eg Gold, Silver etc
	 * @param programName
	 */
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	/**
	 * Eg Gold, Silver etc
	 */ 
	public String getProgramName() {
		return programName;
	}

	public Money[] getRandomBudgets(){
		Money[] m = new Money[Agent.nBudgets]; 
		for(int i = 0; i<Agent.nBudgets; i++){
			if( maxBudget[i] !=null && minBudget[i] !=null){
				m[i] = new Money(minBudget[i].getCurrency(), 
						Math.random()*( maxBudget[i].getValue() - minBudget[i].getValue())+minBudget[i].getValue());
			}
			else
				m[i] = new Money(Default.currency, 0);
		} 
		return m;
	}


	public Money getRandomBudget(int j){
		DoTest.require(j<Agent.nBudgets, "Budget number bigger than "+Agent.nBudgets);

		return new Money("EUR", Math.random()*( maxBudget[j].getValue() - minBudget[j].getValue())+minBudget[j].getValue());

	}
	/**
	 * The identifier of its Agents (RESELLER, FINAL_CUSTOMER etc)
	 * @param identifier
	 */
	public void setIdentifier(MarketSectorType identifier) {
		this.identifier = identifier;
	}
	/**
	 * The identifier of its Agents (RESELLER, FINAL_CUSTOMER etc)
	 */
	public MarketSectorType getIdentifier() {
		return identifier;
	}

}
