/**
 * 
 * @author Mario Alemi
 * @created   Mar 6, 2010
 * @version 0.1
 */
package pmarket;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.strabil.currencies.Money;
import org.strabil.manager.Event;
import org.strabil.manager.RunManager;
import org.strabil.market.Agent;
import org.strabil.market.AgentSet;
import org.strabil.market.MarketFactory;
import org.strabil.market.MarketSectorType;
import org.strabil.market.MktRelationship;
import org.strabil.market.Product;
import org.strabil.market.ProductSet;
import org.strabil.utils.DoTest;

import utils.Default;



public class pMarketFactory implements MarketFactory {
	private ArrayList<LoyaltyLevel> levels;
	private ArrayList<ProductStock> stocks;

	private Event e;

	RunManager rm = RunManager.getInstance();

	
	//private ArrayList<String> levelNames = new ArrayList<String>();

	public pMarketFactory(Event e){
		this.e = e;
	}

	@Override
	public void addAgentSet(AgentSet ll) {
		if(this.levels == null)
			this.levels = new ArrayList<LoyaltyLevel>();
		this.levels.add((LoyaltyLevel)ll);

	}

	@Override
	public Collection<Agent> createAgents(Agent seller) {
		
		ArrayList<Agent> FinalCustomerList  = new ArrayList<Agent>();
		for(LoyaltyLevel ll: this.levels){
			for(int i=0; i< ll.getNumberAgents(); i++){
				Agent pippo = this.e.createAgent();
				pippo.setBudgets(ll.getRandomBudgets());

				seller.addCustomer(pippo);

				//There should be always one relationship, as the customer has just been created.... 
				pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).setProperty("LoyaltyLevel", ll.getProgramName());

				pippo.setIdentifier(ll.getIdentifier().toString());
				FinalCustomerList.add(pippo);
			}
		}
		return FinalCustomerList;
	}


	@Override
	public void addProductSet(ProductSet productSet) {
		if(this.stocks == null)
			this.stocks = new ArrayList<ProductStock>();
		this.stocks.add((ProductStock) productSet);		
	}


	@Override
	public Collection<Product> createProducts(Agent seller) {
		ArrayList<Product> GoodsList = new ArrayList<Product>();

		//		Create goods for each level

		for(ProductStock ps: this.stocks){

			for(int pop =0; pop < ps.getNumberProducts(); pop++){

				Product p = e.createProduct(ps.getName(), 
						ps.getPrice(), 
						ps.getMarketValue(), 
						ps.getFixedCost(), 
						ps.getVariableCost());

				p.getUnderlyingNode().createRelationshipTo(seller.getUnderlyingNode(), MktRelationship.OWNS);

				//Define the owner at which LoyaltyLevel will want to offer the product.
				p.getUnderlyingNode().getSingleRelationship(MktRelationship.OWNS, Direction.OUTGOING).setProperty("LoyaltyLevel", ps.getMarketSectorName());
				GoodsList.add(p);
			}
		}

		/* for(Product p: GoodsList){
			p.printInfo();
		}*/


		return GoodsList;
	}

	public void resetAgentSet() {
		this.levels = new ArrayList<LoyaltyLevel>();
	}

	public ArrayList<LoyaltyLevel> getAgentSets(){
		return this.levels;
	}

	public void setAgentSets(ArrayList<LoyaltyLevel> levels){
		this.levels = levels;
	}

	public void resetProductSet() {
		this.stocks = new ArrayList<ProductStock>();
	}

	public ArrayList<ProductStock> getProductSets() {
		return stocks;
	}

	public void setProductSets(ArrayList<ProductStock> stocks) {
		this.stocks = stocks;
	}



	/**
	 * It creates an ArrayList (Collection in the Interface) of LoyaltyLevel's from the customerList. 
	 * LoyaltyLevel's are defined by a single budget -ie Agent's are in a certain Level only regarding a certain
	 * budget. Then to create new Levels we have to tell the method which budget keyBudget is going to be 
	 * considered for putting each Agent in the right level.
	 * 
	 * After all the market's activity some Agent has decided to spend more, some less, and these will move
	 * up or down...
	 * 
	 * TODO *NB* this.levels of the instantiated object are not touched! Maybe it should be re-populateAgentSets, and should 
	 * and change the parameters of this.levels
	 * *************************************************************
	 * TODO *********THIS METHOD HAS TO BE CHECKED******************
	 * *************************************************************
	 * @param customerList
	 * @param keyBudget
	 * @param period
	 */
	public Collection<LoyaltyLevel> populateAgentSets(ArrayList<Agent> customerList, CustomerBudget keyBudget, long period){

		int nLevels = this.levels.size();
		int[] numberAgents = new int[nLevels];
		int kb = keyBudget.ordinal();

		ArrayList<LoyaltyLevel> mylevels = new ArrayList<LoyaltyLevel>(nLevels);
		Money[][] aveBudget = new Money[nLevels][Agent.nBudgets];

		//Initialize all to 0
		for(int l=0; l<nLevels; l++){
			numberAgents[l] = 0;
			for(int b=0; b<Agent.nBudgets; b++)
				aveBudget[l][b] = new Money(Default.currency, 0.0);
		}


		//Cycle over all agents
		for(Agent pippo: customerList){
			//For each agent cycle over the levels, when finds the right one (judging on a certain budget)
			searchlevel: for(LoyaltyLevel ll: this.levels){

				int l =   this.levels.indexOf(ll);
				//Right budget?
				if(pippo.getBudget(kb).getValue() < ll.getMaxBudget(kb).getValue() 
						&& pippo.getBudget(kb).getValue() >= ll.getMinBudget(kb).getValue() ){
					//DoTest.debug(l+" A-----------"+numberAgents[l]);
					numberAgents[l]++;
					//DoTest.debug(l+" B-----------"+numberAgents[l]);
					for(int b=0; b<Agent.nBudgets; b++){
						aveBudget[l][b] =  pippo.getBudget(b).add(aveBudget[l][b]);
					}
					break searchlevel;
				}

			}
		}
		
		
		for(LoyaltyLevel ll: this.levels){
			String name = ll.getProgramName();
			int l = this.levels.indexOf(ll);

			//DoTest.debug("numberAgents["+l+"] = "+numberAgents[l]);
			//Normalize average budget
			for(int b=0; b<Agent.nBudgets; b++){
				aveBudget[l][b].divAssign(numberAgents[l]);
			}

			mylevels.add(new LoyaltyLevel());
			mylevels.get(l).setProgramName(name);
			mylevels.get(l).setNumberAgents(numberAgents[l]);
			mylevels.get(l).setPeriod(period);
			mylevels.get(l).setAveBudget(aveBudget[l]);
			mylevels.get(l).setMinBudget(ll.getMinBudget());
			mylevels.get(l).setMaxBudget(ll.getMaxBudget());
		}
		
		return mylevels;
	}


	public ArrayList<String> getLevelNames(){
		ArrayList<String> ass = new ArrayList<String>();
		for(LoyaltyLevel ll: levels){
			ass.add(ll.getProgramName());
		}
		return ass;
	}

	private Money[][] setDifferentialBudgets(Money[][] bdg){


		//Each customer must know how much is the loyalty offer difference compared to richer and poorer customers
		for(int l=0; l<levels.size(); l++){
			//DIFFERENCE_UP
			try{
				bdg[l][CustomerBudget.DIFFERENCE_UP.ordinal()] = bdg[l+1][CustomerBudget.LOYALTY_PRESENT.ordinal()]
				                                                          .sub(bdg[l][CustomerBudget.LOYALTY_PRESENT.ordinal()]);				
			} catch (ArrayIndexOutOfBoundsException e) {
				bdg[l][CustomerBudget.DIFFERENCE_UP.ordinal()] = new Money("EUR",0);
			}

			//DIFFERENCE_DOWN
			try{
				bdg[l][CustomerBudget.DIFFERENCE_DOWN.ordinal()] = bdg[l-1][CustomerBudget.LOYALTY_PRESENT.ordinal()]
				                                                            .sub(bdg[l][CustomerBudget.LOYALTY_PRESENT.ordinal()]);				
			} catch (ArrayIndexOutOfBoundsException e) {
				bdg[l][CustomerBudget.DIFFERENCE_DOWN.ordinal()] = new Money("EUR",0);
			}
		}
		return bdg;
	}

	/**
	 * Create a dummy list of {@link ProductStock}'s
	 */
	public void createConfigProductStock(){

		this.stocks = new ArrayList<ProductStock>();
		String[] productName = {"Consulting", "WebSite"};
		int nProd = productName.length;

		Money[] prices = {new Money("EUR",10), new Money("EUR",150)};
		Money[] values = {new Money("EUR",90), new Money("EUR",150)};

		for(int i=0; i<nProd; i++){

			ProductStock s = new ProductStock();
			s.setName(productName[i]);
			s.setMarketSectorName(""); //It is going to be offered to everybody
			s.setNumberProducts(this.getTotalCustomers());
			s.setPrice(prices[i]);
			s.setMarketValue(values[i]);
			this.stocks.add(s);
		}
	}
	/**
	 * Create a dummy list of {@link LoyaltyLevel}'s
	 */
	public void createConfigLoyaltyLevel(){

		ArrayList<String> levelNames = new ArrayList<String>();
		ArrayList<Integer> levelPopulation = new ArrayList<Integer>();
		
		int nLevels = Integer.parseInt(rm.getProperty("NumberLevels"));
		for(int i =0; i<nLevels; i++){
			levelNames.add(rm.getProperty("Level"+(i+1)));
			levelPopulation.add( Integer.parseInt( rm.getProperty("Population"+(i+1))));
		}

		this.levels = new ArrayList<LoyaltyLevel>();
		MarketSectorType identifier = MarketSectorType.RESELLER;

		//==   Budgets[iLevel][iBudget] ==\\
		Money[][] minBudgets = {
				//GOODS, SERVICES, LOYALTY PRESENT, PRESENT UP, PRESENT DOWN
				{new Money("EUR",300), new Money("EUR",300), new Money("EUR",300),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",500), new Money("EUR",500), new Money("EUR",500),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",700), new Money("EUR",700), new Money("EUR",700),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",900), new Money("EUR",900), new Money("EUR",900),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",1000), new Money("EUR",1000), new Money("EUR",1000),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)}};

		Money[][] maxBudgets = {
				{new Money("EUR",500), new Money("EUR",500), minBudgets[0][2],    new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",700), new Money("EUR",700),  minBudgets[1][2],   new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",900), new Money("EUR",900),  minBudgets[2][2],   new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",1000), new Money("EUR",1000),  minBudgets[3][2], new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)},
				{new Money("EUR",1500), new Money("EUR",1500),  minBudgets[4][2], new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0),new Money("EUR", 0)}};


		minBudgets = this.setDifferentialBudgets(minBudgets);
		maxBudgets = this.setDifferentialBudgets(maxBudgets);

		Money[][] aveBudget = new Money[levelNames.size()][Agent.nBudgets];

		//Create LL and set properties 
		for(int l=0; l< levelNames.size(); l++){

			for(int b=0;b<Agent.nBudgets;b++){
				aveBudget[l][b] = (minBudgets[l][b].add(maxBudgets[l][b]).divAssign(2.0));
			}


			LoyaltyLevel ll = new LoyaltyLevel();
			ll.setProgramName(levelNames.get(l));
			ll.setMinBudget(minBudgets[l]);
			ll.setMaxBudget(maxBudgets[l]);
			ll.setAveBudget(aveBudget[l]);
			ll.setNumberAgents(levelPopulation.get(l));
			ll.setIdentifier(identifier);
			ll.setPeriod(0);


			this.levels.add(ll);
		}
	}

	//oooooOOOOOO000 Private Methods  000OOOOOOoooooo\\

	private int getTotalCustomers(){
		int c=0;

		for(LoyaltyLevel ll: levels)
			c=c + ll.getNumberAgents();
		return c;
	}


}
