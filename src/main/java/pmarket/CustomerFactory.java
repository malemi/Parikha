/**
 * 
 */
package pmarket;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.graphdb.Direction;

import utils.DoTest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;

import currencies.Money;

import manager.Event;
import manager.SimRelationship;
import market.Agent;
import market.AgentFactory;
import market.MktRelationship;
import market.Product;

/**
 * The developer has to create the {@link LoyaltyLevel} and the {@link ProductStock} either through
 * {@link #createDummyLoyaltyLevel()} and {@link #createDummyProductStock()} or reading the objects' 
 * serialized Strings with {@link #deserializeLoyaltyLevel(String, String)}
 * 
 * 
 * TODO mplements agentfactory and productfactory?
 * @author Mario Alemi
 */
public class CustomerFactory {

	private Collection<LoyaltyLevel> levels;
	private Collection<ProductStock> stocks;

	private Event e;

	//Only for dummy market:
	private String[] dummyLevelName= { "NoStatus", "Brass", "Bronze", "Silver", "Gold"};
	private int nLevels = dummyLevelName.length;
	//End

	public CustomerFactory(Event e){
		this.e = e;
	}


	/*
	 * TODO protocol must be a enum
	 */
	public String serializeLoyaltyLevels(String protocol){
		if(protocol == "XML")
			return this.toXML(this.levels);
		else if(protocol == "JSON")
			return this.toJSON(this.levels);
		else 
			return "CustomerFactory.flushAgents: protocol "+protocol+" not recognized. Only XML and JSON at the moment.";

	}

	/**
	 * 
	 * 
	 * @param serialString
	 * @param protocol
	 */
	@SuppressWarnings("unchecked")
	public void deserializeLoyaltyLevel(String serialString, String protocol){
		if(protocol == "XML")
			this.levels = (Collection<LoyaltyLevel>) this.fromXML(serialString);
		else if(protocol == "JSON")
			this.levels = (Collection<LoyaltyLevel>) this.fromJSON(serialString, LoyaltyLevel.class);
		else
			DoTest.warn( "CustomerFactory.flushAgents: protocol "+protocol+" not recognized. Only XML and JSON at the moment.");
	}


	/**
	 * @see market.AgentFactory#createAgents()
	 */
	public Collection<Agent> createAgents(Agent seller) {
		ArrayList<Agent> FinalCustomerList  = new ArrayList<Agent>();

		for(LoyaltyLevel ll: this.levels){
			for(int i=0; i< ll.getSize(); i++){
				Agent pippo = this.e.createAgent();
				seller.addCustomer(pippo);

				//There should be always one relationship, as the customer has just been created.... 
				pippo.getUnderlyingNode().getSingleRelationship(MktRelationship.IS_CUSTOMER, Direction.OUTGOING).setProperty("LoyaltyLevel", ll.getProgramName());

				pippo.setBudgets(ll.getRandomBudgets());
				pippo.setIdentifier(ll.getIdentifier());
				FinalCustomerList.add(pippo);
			}
		}
		return FinalCustomerList;
	}


	public Collection<Product> createProducts(Agent seller){
		ArrayList<Product> GoodsList = new ArrayList<Product>();

		//		Create goods for each level

		for(ProductStock ps: this.stocks){

			for(int pop =0; pop < ps.getSize(); pop++){

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


	public void createDummyProductStock(){

		this.stocks = new ArrayList<ProductStock>();
		String[] productName = {"Aspirin", "Vitamin"};
		int nProd = productName.length;

		Money[] prices = {new Money("EUR",10), new Money("EUR",150)};
		Money[] values = {new Money("EUR",90), new Money("EUR",150)};

		for(int i=0; i<nProd; i++){

			ProductStock s = new ProductStock();
			s.setName(productName[i]);
			s.setMarketSectorName(""); //It is going to be offered to everybody
			s.setSize(this.getTotalCustomers());
			s.setPrice(prices[i]);
			s.setMarketValue(values[i]);
			this.stocks.add(s);
		}
	}

	public void createDummyLoyaltyLevel(){
		DoTest.warn("Creating Dummy Agent Loyalty Levels");
		//t=0 parameters
		int[] level_population = {15, 4, 5, 7, 3};

		this.levels = new ArrayList<LoyaltyLevel>();
		String identifier = "Reseller";
		//Budgets[iLevel][iBudget]
		Money[][] minBudgets = {
				{new Money("EUR",300), new Money("EUR",300), new Money("EUR",300),null,null,null,null,null,null},
				{new Money("EUR",500), new Money("EUR",500), new Money("EUR",500),null,null,null,null,null,null},
				{new Money("EUR",700), new Money("EUR",700), new Money("EUR",700),null,null,null,null,null,null},
				{new Money("EUR",900), new Money("EUR",900), new Money("EUR",900),null,null,null,null,null,null},
				{new Money("EUR",1000), new Money("EUR",1000), new Money("EUR",1000),null,null,null,null,null,null}};

		Money[][] maxBudgets = {
				{new Money("EUR",500), new Money("EUR",500), new Money("EUR",500),null,null,null,null,null,null},
				{new Money("EUR",700), new Money("EUR",700), new Money("EUR",700),null,null,null,null,null,null},
				{new Money("EUR",900), new Money("EUR",900), new Money("EUR",900),null,null,null,null,null,null},
				{new Money("EUR",1000), new Money("EUR",1000), new Money("EUR",1000),null,null,null,null,null,null},
				{new Money("EUR",1500), new Money("EUR",1500), new Money("EUR",1500),null,null,null,null,null,null}};

		//Create LL and set properties 
		for(int l=0; l<this.nLevels; l++){
			LoyaltyLevel ll = new LoyaltyLevel();
			ll.setProgramName(dummyLevelName[l]);
			ll.setMinBudget(minBudgets[l]);
			ll.setMaxBudget(maxBudgets[l]);
			ll.setSize(level_population[l]);
			ll.setIdentifier(identifier);
			this.levels.add(ll);
		}
	}

	private String toXML(Object obj){
		XStream xs = new XStream();

		StringWriter sw =  new StringWriter();
		xs.toXML(obj, sw);

		return sw.toString();

		/*FileOutputStream fs;

		try {
			fs = new FileOutputStream("LoyaltyLevel.xml");
			xs.toXML(this.levels, fs);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 */

	}

	private Object fromXML(String serialString) {
		XStream xs = new XStream();

		return xs.fromXML(serialString);

	}

	@SuppressWarnings("unchecked")
	private Object fromJSON(String serialString, Class type){
		//http://sites.google.com/site/gson/gson-user-guide
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if(type == LoyaltyLevel.class){
			Type collectionType = new TypeToken<Collection<LoyaltyLevel>>(){}.getType();
			return gson.fromJson(serialString, collectionType );
		} else if(type == ProductStock.class){
			Type collectionType = new TypeToken<Collection<ProductStock>>(){}.getType();
			return gson.fromJson(serialString, collectionType );
		} else {
			DoTest.warn("CustomerFactory.fromJSON: Type "+type+" not valid. Retruning null.");
			return null;
		}

		/*
		try {
			FileReader fi = new FileReader("LoyaltyLevel.json");
			BufferedReader br = new BufferedReader(fi); 
			String s; 
			while((s = br.readLine()) != null) { 

			} 


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */

	}

	private String toJSON(Object obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();



		/*try {
			FileOutputStream fs = new FileOutputStream("ProductStock.json");
			PrintStream p = new PrintStream(fs);
			p.println(gson.toJson(this.levels));

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		 */
		return gson.toJson(obj);

	}


	private int getTotalCustomers(){
		int c=0;

		for(LoyaltyLevel ll: levels)
			c=c+ll.getSize();
		return c;
	}


}
