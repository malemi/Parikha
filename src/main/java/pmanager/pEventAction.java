package pmanager;

/*import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 */
import java.util.ArrayList;

import org.neo4j.graphdb.Direction;

import currencies.Money;

import pmarket.CustomerFactory;
import pmarket.OutputLibrary;

import manager.*;
import market.*;
import math.InverseCumulativeNormal;
import pmarket.*;
import reaction.SimpleReaction;
import utils.DoTest;

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
	
	//For the up/down
	InverseCumulativeNormal invNorm;

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
		myCF.deserializeLoyaltyLevel(myCF.serializeLoyaltyLevels(OutputLibrary.GSON), OutputLibrary.GSON);

		//System.out.println(	myCF.serializeLoyaltyLevels(OutputLibrary.XStream));
		System.out.println(	myCF.serializeLoyaltyLevels(OutputLibrary.XStream));

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
				//Start looking for Services's
				for(Product service: seller.getOwnedProducts()){
					//As soon as we see a service with the right LoyaltyLevel
					//the product is offered also if it has empty LoyaltyLevel property
					String service_ll = (String) service.getUnderlyingNode().getSingleRelationship(MktRelationship.OWNS, Direction.OUTGOING).getProperty("LoyaltyLevel");
					if(service_ll == pippo_ll ||
							service_ll == "" ){
						//System.out.println("Evaluating...");
						//...we offer it to the customer
						if(pippo.evaluates(service, seller, reaction)){
							pippo.buys(service);
							pippo.subFromBudget(AgentBudgets.ONE.ordinal(), service.getPrice());
							seller.addToBudget(AgentBudgets.ONE.ordinal(), service.getPrice());

							DoTest.debug(pippo.getIdentifier()+"_"+pippo.getUId()+" bought "+service.getIdentifier()+"_"+service.getUId());
							continue offer;
						}
					}
				}
			}

		for(Agent pippo: this.FinalCustomerList){
			pippo.setBudget(CustomerBudget.GOODS.ordinal(), 
					pippo.getBudget(CustomerBudget.GOODS.ordinal())
					.mul(1-Double.parseDouble(RunManager.getInstance().properties().getProperty("naturalLoss"))));

			//At the moment works like that: 10% of the upgradePotential will go up if the difference is gammaLow10*Budget.GOODS -->
			// Also note: a Customer which is close to the bottom of the level does not care about the fact that next year will 
			//receive less. He will care next year.
			
			//upgradePotential: is the Customer in the upgradePotential?
			if(Math.random() < Double.parseDouble(RunManager.getInstance().properties().getProperty("upgradePotential")) ){
			
			//Below we have a gaussian which says that if we offer a difference of gammaLow/Up*budget.GOODS to a 
			//Customer there is 10%/90% probability for him/her to jump to the higher level.
			
			Double ave = pippo.getBudget(CustomerBudget.GOODS.ordinal())
			.mul( (Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaLow10"))+
					Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaUp10")))/2.0)
					.getValue();
			Double sigma = pippo.getBudget(CustomerBudget.GOODS.ordinal())
			.mul( (Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaUp10"))-
					Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaLow10")))/2.6)
					.getValue(); //TODO Check 10%=a 1.3sigma
			
			invNorm = new InverseCumulativeNormal(ave, sigma);
			if(Math.random() < invNorm.op(pippo.getBudget(CustomerBudget.DIFFERENCE_UP.ordinal()).getValue() )  )
				pippo.setBudget(CustomerBudget.GOODS.ordinal(), 
				//Random budget from the upper level	
						new Money("EUR",666.6)
				/*
				 * qui si capisce che i vari livelli della factory devono essere accessibili, se non altro per
				 * poter poi essere "ricostruiti" a partire dalla CustomerList.
				 *=> qui metto un livello.randomBudget
				 * 
				 */
				);
			
			}
			
		}
		return true;

	}



}
