/**
* This class will go in Strabil.Utils 
 * To be renamed AgentSet 
 */



package pmarket;
import java.io.Serializable;

import utils.DoTest;

import market.Agent;
import market.MarketSectorType;

import currencies.Money;


public class LoyaltyLevel implements Serializable {

	private static final long serialVersionUID = 2095384450109433717L;
	private int numberAgents;
	private Money[] minBudget = new Money[Agent.nBudgets];
	private Money[] maxBudget = new Money[Agent.nBudgets];
	private String programName;
	private MarketSectorType identifier;//ENUM
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
				m[i] = new Money("EUR", 
						Math.random()*( maxBudget[i].getValue() - minBudget[i].getValue())+minBudget[i].getValue());
			}
			else
				m[i] = new Money("EUR", 0);

		} 
		return m;
	}


	public Money getRandomBudget(int j){
		DoTest.require(j<Agent.nBudgets, "Budget number bigger than "+Agent.nBudgets);

		return new Money("EUR", Math.random()*( maxBudget[j].getValue() - minBudget[j].getValue())+minBudget[j].getValue());

	}
	/**
	 * The identifier of the Agents
	 * @param identifier
	 */
	public void setIdentifier(MarketSectorType identifier) {
		this.identifier = identifier;
	}
	/**
	 * The identifier of the Agents
	 * @return identifier
	 */
	public MarketSectorType getIdentifier() {
		return identifier;
	}

}
