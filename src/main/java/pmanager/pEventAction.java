package pmanager;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.neo4j.graphdb.Direction;

import pmarket.CustomerFactory;

import currencies.Money;

import manager.*;
import market.*;
import pmarket.*;
import reaction.SimpleReaction;
import utils.DoTest;

/**
 * 
 */

/**
 * @author Mario Alemi
 *
 */
public class pEventAction implements EventAction{	
	//Seller, Customers and Products
	Agent seller;
	ArrayList<Agent> FinalCustomerList;
	ArrayList<Product> DrugsList;
	private CustomerFactory myCF;

	public boolean BeginEventAction(Event e) {

		//Creating Agents//
		//Seller....
		seller = e.createAgent("BigBigSeller");

		//...and customers
		//		FinalCustomerList  = new ArrayList<Agent>();
		myCF = new CustomerFactory(e);
		myCF.createDummyLoyaltyLevel();
		myCF.createDummyProductStock();

		FinalCustomerList = (ArrayList<Agent>) myCF.createAgents(seller);

		//		System.out.println(myCF.serializeLoyaltyLevels("XML"));
		//TEST
		myCF.deserializeLoyaltyLevel(myCF.serializeLoyaltyLevels("JSON"), "JSON");
		System.out.println(	myCF.serializeLoyaltyLevels("JSON"));

		//		Create goods for each level
		DrugsList  = (ArrayList<Product>) myCF.createProducts(seller);


		return true;
	}

	public boolean EndEventAction(Event e) {
		// Auto-generated method stub
		return true;
	}
	
	public boolean StepAction(int i, Event e) {
		DoTest.require(i>0, "Event.StepAction: Number of periods < 0");
		switch(i){
		case 0:
			return true;
		case 1:
			return this.DoOneCycle();
		default:
			return this.DoOneCycle();
		}
	}

	/**
	 * Get all Agents IS_CUSTOMER PAH, takes the relationship property LoyaltyLevel.
	 * Loops over DrugsList and as soon as it finds  the right LoyaltyLevel it offers it to the 
	 * correspondent Agent.
	 * @return
	 */
	private boolean DoOneCycle(){
		//Reaction to be used during this cycle
		SimpleReaction reaction = new SimpleReaction();

		//Cycle over all customers
		offer:	
			for(Agent pippo: this.FinalCustomerList){
				//Record the LoyaltyLevel of the customer
				String pippo_ll = (String) pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).getProperty("LoyaltyLevel");
				//pippo.printInfo();
				//Start looking for PAH droga's
				for(Product droga: seller.getOwnedProducts()){
					//As soon as we see a droga with the right LoyaltyLevel
					//the product is offered also if it has empty LoyaltyLevel property
					String droga_ll = (String) droga.getUnderlyingNode().getSingleRelationship(MktRelationship.OWNS, Direction.OUTGOING).getProperty("LoyaltyLevel");
					if(droga_ll == pippo_ll ||
							droga_ll == "" ){
						//System.out.println("Evaluating...");
						//...we offer it to the customer
						if(pippo.evaluates(droga, seller, reaction)){
							pippo.buys(droga);
							pippo.subFromBudget(CustomerBudgets.ONE.ordinal(), droga.getPrice());
							System.out.println(pippo.getIdentifier()+"_"+pippo.getUId()+" bought "+droga.getIdentifier()+"_"+droga.getUId());
							continue offer;
						}
					}
				}
			}
		return true;
	}
}
