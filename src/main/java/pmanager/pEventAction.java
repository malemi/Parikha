package pmanager;

/*import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 */
import java.util.ArrayList;
import java.util.Hashtable;

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
import org.strabil.utils.DoTest;

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

	@Override
	public boolean BeginEventAction(Event e) {

		xs = new XStream();
		xs.processAnnotations(LoyaltyLevel.class);

		//Analysis manager:
		myAM = new pAnalysisManager();
		
		//Creating Agents//
		//Seller....
		seller = e.createAgent("BigBigSeller");

		//...and customers
		myMF = new pMarketFactory(e);
		myMF.createDummyLoyaltyLevel();
		myMF.createDummyProductStock();

		FinalCustomerList = (ArrayList<Agent>) myMF.createAgents(seller);

		//		System.out.println(myCF.serializeLoyaltyLevels("XML"));
		//TEST
		//myCF.deserializeLoyaltyLevel(myCF.serializeLoyaltyLevels(OutputLibrary.XStream), OutputLibrary.XStream);
		//		System.out.println(	xs.toXML(this.myCF.getLevels()));
		System.out.print(this.myAM.printAgentSets(this.myMF.getAgentSets(), separator));

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
		DoTest.require(period>0, "Event.StepAction: Number of periods < 0");


		switch(period){
		case 0:
			return true;
		default:
			boolean c = this.DoOneCycle();
			//System.out.println( xs.toXML(this.myCF.createLoyaltyLevels(this.FinalCustomerList, CustomerBudget.GOODS, period)    )	);
			System.out.print(this.myAM.printAgentSets( this.myMF.populateAgentSets(this.FinalCustomerList,
					CustomerBudget.GOODS, period) ,"\t"  )  );			
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
							pippo.subFromBudget(CustomerBudget.GOODS.ordinal(), service.getPrice());

							seller.addToBudget(AgentBudgets.ONE.ordinal(), service.getPrice());

							//							DoTest.debug(pippo.getIdentifier()+" "+pippo.getUId()+" bought "+service.getIdentifier()+" "+service.getUId()
							//								+" for "+service.getPrice().getValue()+" EUR, new Budget.GOODS = "+Math.round(pippo.getBudget(CustomerBudget.GOODS.ordinal()).getValue()));

							continue offer;
						}
					}
				}
			}

		//Natural loss and possible jump
		for(Agent pippo: this.FinalCustomerList){
			pippo.setBudget(CustomerBudget.GOODS.ordinal(), 
					pippo.getBudget(CustomerBudget.GOODS.ordinal())
					.mul(1-Double.parseDouble(RunManager.getInstance().properties().getProperty("naturalLoss"))));
			String customerProgram = (String) pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).getProperty("LoyaltyLevel") ;

			int levelIndex = myMF.getLevelNames().indexOf(customerProgram);

			//Only if the customer is not on the uppest level
			if( (levelIndex < myMF.getLevelNames().size()-1 )){


				//At the moment works like that: 10% of the upgradePotential will go up if the difference is gammaLow10*Budget.GOODS -->
				// Also note: a Customer which is close to the bottom of the level does not care about the fact that next year will 
				//receive less. He will care next year.

				//upgradePotential: is the Customer in the upgradePotential?
				if(Math.random() < Double.parseDouble(RunManager.getInstance().properties().getProperty("upgradePotential")) ){

					//min. max and average are referred to the LoyaltyLevel program the customer is in.
					Money min;
					Money max;
					Money averageBudget;
					String levelProgram = myMF.getLevelNames().get(levelIndex);
					String upperLevelProgram =  myMF.getLevelNames().get(levelIndex+1);

					min = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets()).get(levelIndex).getMinBudget(CustomerBudget.GOODS.ordinal());
					max = ((ArrayList<LoyaltyLevel>) myMF.getAgentSets()).get(levelIndex).getMaxBudget(CustomerBudget.GOODS.ordinal());
					averageBudget = new Money (min.getCurrency(), (min.getValue()+max.getValue())*0.5 );



					DoTest.ensure(!(min==null || max == null || averageBudget == null || levelProgram == ""), "Something wrong in pEventAction, min/max/average Budgets....)");
					//Below we have a gaussian which says that if we offer a difference of gammaLow/Up*AverageBudget.GOODS to a 
					//Customer there is 10%/90% probability for him/her to jump to the higher level.
					//It had to be the budget of the Level, and not the customer of course, as otherwise as the 
					//customer buys his budget decrease the probability of jumping would increase.

					Double ave = averageBudget.getValue() *
					( (Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaLow10"))+
							Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaUp10")))/2.0) ;

					Double sigma = averageBudget.getValue() *
					( (Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaUp10"))-
							Double.parseDouble(RunManager.getInstance().properties().getProperty("gammaLow10")))/2.6); //TODO Check 10%=a 1.3sigma

					//DoTest.debug("Average: "+ave+", sigma: "+sigma);

					invNorm = new InverseCumulativeNormal(ave, sigma);

					if(pippo.getBudget(CustomerBudget.DIFFERENCE_UP.ordinal()).getValue()   < invNorm.op(Math.random() ) ){
						//	DoTest.debug(pippo.getIdentifier()+"_"+pippo.getUId() +" decided to jump to "+upperLevelProgram +".");

						//Random budget from the upper level
						pippo.setRandomBudget(CustomerBudget.GOODS.ordinal(), min, max);
						pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).setProperty("LoyaltyLevel", upperLevelProgram);


					}
				}
			}
		}
		return true;
	}
}
