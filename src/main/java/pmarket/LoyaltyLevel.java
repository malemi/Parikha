package pmarket;
import java.io.Serializable;

import utils.DoTest;

import market.Agent;

import currencies.Money;


public class LoyaltyLevel implements Serializable {

	private static final long serialVersionUID = 2095384450109433717L;
	private int size;
	private Money[] minBudget = new Money[Agent.nBudgets];
	private Money[] maxBudget = new Money[Agent.nBudgets];
	private String programName;
	private String identifier;
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

	public void setSize(int size) {
		this.size = size;
	}
	public int getSize() {
		return size;
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
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	/**
	 * The identifier of the Agents
	 * @return identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

}
