package pmanager;

import java.util.ArrayList;


import org.neo4j.graphdb.Direction;
import org.strabil.currencies.Money;
import org.strabil.manager.Event;
import org.strabil.manager.EventAction;
import org.strabil.manager.RunManager;
import org.strabil.market.Agent;
import org.strabil.market.MktRelationship;
import org.strabil.market.Product;
import org.strabil.math.InverseCumulativeNormal;
import org.strabil.reaction.SimpleReaction;
import static org.strabil.utils.DoTest.*;
import static pmarket.CustomerBudget.*;

import com.thoughtworks.xstream.XStream;


import pmarket.*;

/**
 * @author Mario Alemi
 *
 */
public class pEventAction implements EventAction{	
	//Seller, Customers and Products
	Agent seller;
	ArrayList<Agent> FinalCustomerList;
	ArrayList<Product> DrugsList;
	private pMarketFactory myMF;
	private pAnalysisManager myAM;
	private XStream xs;
	private String separator = "\t";

	//For the up/down
	InverseCumulativeNormal invNorm;
	RunManager rm = RunManager.getInstance();
	
	//Configuration parameters
	double naturalLoss;
	double upgradePotential;
	double gl10;
	double gu10;

	
	@Override
	public boolean BeginEventAction(Event e) {
		naturalLoss = Double.parseDouble(rm.getProperty("naturalLoss"));
		upgradePotential = Double.parseDouble(rm.getProperty("upgradePotential"));
		gl10 = Double.parseDouble(rm.getProperty("gammaLow10"));
		gu10 = Double.parseDouble(rm.getProperty("gammaUp10"));	
		
		xs = new XStream();
		xs.processAnnotations(LoyaltyLevel.class);

		//Analysis manager:
		myAM = new pAnalysisManager();

		//Creating Agents//
		//Seller....
		seller = e.createAgent("BigBigSeller");

		//...and customers
		myMF = new pMarketFactory(e);
		myMF.createConfigLoyaltyLevel();
		myMF.createConfigProductStock();

		FinalCustomerList = (ArrayList<Agent>) myMF.createAgents(seller);

		//		System.out.println(myCF.serializeLoyaltyLevels("XML"));
		//TEST
		//myCF.deserializeLoyaltyLevel(myCF.serializeLoyaltyLevels(OutputLibrary.XStream), OutputLibrary.XStream);
		//		System.out.println(	xs.toXML(this.myCF.getLevels()));
		//System.out.print(this.myAM.printAgentSets(this.myMF.getAgentSets(), separator));
		info(myAM.printAgentsHeader(separator)+"\n");
		info(myAM.printAgents(FinalCustomerList, 0, separator));
		//System.out.println(	myCF.serializeLoyaltyLevels(OutputLibrary.XStream));

		//		Create goods for each level
		DrugsList  = (ArrayList<Product>) myMF.createProducts(seller);

		return true;
	}

	@Override
	public boolean EndEventAction(Event e) {
		// Auto-generated method stub
		return true;
	}

	@Override
	public boolean StepAction(int period, Event e) {
		require(period>0, "Event.StepAction: Number of periods < 0");


		switch(period){
		case 0:
			return true;
		default:
			boolean c = this.DoOneCycle();
			//System.out.println( xs.toXML(this.myCF.createLoyaltyLevels(this.FinalCustomerList, CustomerBudget.GOODS, period)    )	);
			//System.out.print(this.myAM.printAgentSets( this.myMF.populateAgentSets(this.FinalCustomerList,
			//		CustomerBudget.GOODS, period) ,"\t"  )  );
			info(myAM.printAgents(FinalCustomerList, period, separator));

			return c;
		}
	}

	/**
	 * Get all Agents IS_CUSTOMER PAH, takes the relationship property LoyaltyLevel.
	 * Loops over DrugsList and as soon as it finds  the right LoyaltyLevel in a Product will offer it to the 
	 * correspondent Agent.
	 * @return
	 */
	private boolean DoOneCycle(){
		//Reaction to be used during this cycle
		SimpleReaction reaction = new SimpleReaction();
		//DoTest.debug("=======================================================================");
		//Cycle over all customers
		offer:	
			for(Agent pippo: this.FinalCustomerList){
				//LoyaltyLevel name of the customer
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
							pippo.subFromBudget(CustomerBudget.GOODS.ordinal(), service.getPrice());

							seller.addToBudget(AgentBudgets.ONE.ordinal(), service.getPrice());

							//							DoTest.debug(pippo.getIdentifier()+" "+pippo.getUId()+" bought "+service.getIdentifier()+" "+service.getUId()
							//								+" for "+service.getPrice().getValue()+" EUR, new Budget.GOODS = "+Math.round(pippo.getBudget(CustomerBudget.GOODS.ordinal()).getValue()));

							continue offer;
						}
					}
				}
			}
		////////////////\\\\\\\\\\\\\\\\
		//Natural loss and possible jump\\
		//////////////////\\\\\\\\\\\\\\\\\\
		customerloop:
			for(Agent pippo: this.FinalCustomerList){

				pippo.setBudget(GOODS.ordinal(), 
						pippo.getBudget(GOODS.ordinal())
						.mul(1-naturalLoss));

				//LoyaltyLevel name of the customer
				String pippo_ll = (String) pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).getProperty("LoyaltyLevel") ;

				int levelIndex = myMF.getLevelNames().indexOf(pippo_ll);

				//Only if the customer is not on the uppest level
				if( (levelIndex < myMF.getLevelNames().size()-1 )){


					//At the moment works like that: 10% of the upgradePotential will go up if the difference is gammaLow10*Budget.GOODS -->
					// FIXME A Customer which is close to the bottom of the level does not care about the fact that next year will 
					//receive less. 

					//upgradePotential: is the Customer in the upgradePotential?
					if(Math.random() < upgradePotential ){

						//min. max and average are referred to the LoyaltyLevel program the customer is in.
						Money averageBudget;
						String levelProgram = myMF.getLevelNames().get(levelIndex);
						String upperLevelProgram =  myMF.getLevelNames().get(levelIndex+1);

						Money min = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets()).get(levelIndex).getMinBudget(GOODS.ordinal());
						Money max = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets()).get(levelIndex).getMaxBudget(GOODS.ordinal());

						averageBudget = new Money (min.getCurrency(), (min.getValue()+max.getValue())*0.5 );

						ensure(!(min==null || max == null || averageBudget == null || levelProgram == ""), "Something wrong in pEventAction, min/max/average Budgets....)");

						//Below we have a gaussian which says that if we offer a difference of gammaLow/Up*AverageBudget.GOODS to a 
						//Customer there is 10%/90% probability for him/her to jump to the higher level.
						//It had to be the budget of the Level, and not the customer of course, as otherwise as the 
						//customer buys his budget decrease the probability of jumping would increase.


						Double ave = averageBudget.getValue() * (gl10+gu10)/2.0;
						Double sigma = averageBudget.getValue() * (gu10-gl10)/2.6; //TODO Check 10%=a 1.3sigma
						//OK debug("Level: "+pippo_ll+" Gaussian Average: "+ave+", sigma: "+sigma);

						invNorm = new InverseCumulativeNormal(ave, sigma);

						if(pippo.getBudget(CustomerBudget.DIFFERENCE_UP.ordinal()).getValue()   < invNorm.op(Math.random() ) ){
							//	DoTest.debug(pippo.getIdentifier()+"_"+pippo.getUId() +" decided to jump to "+upperLevelProgram +".");

							Money minUp = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets())
							.get(levelIndex+1).getMinBudget(GOODS.ordinal());
							Money maxUp = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets())
							.get(levelIndex+1).getMaxBudget(GOODS.ordinal());

							//Random budget from the upper level
							pippo.setRandomBudget(GOODS.ordinal(), minUp, maxUp);
						}
					}
				}

				//Reassign to each customer the right LoyaltyLevel
				searchlevel: for(LoyaltyLevel ll: myMF.getAgentSets()){

					int l =    myMF.getAgentSets().indexOf(ll);
					//Right budget?
					if(pippo.getBudget(GOODS.ordinal()).getValue() < ll.getMaxBudget(GOODS.ordinal()).getValue() 
							&& pippo.getBudget(GOODS.ordinal()).getValue() >= ll.getMinBudget(GOODS.ordinal()).getValue() ){

						pippo.getUnderlyingNode()
						.getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING)
						.setProperty("LoyaltyLevel", myMF.getLevelNames().get(l));
						 

						break searchlevel;
					}

				}



			}
			return true;
	}
}
