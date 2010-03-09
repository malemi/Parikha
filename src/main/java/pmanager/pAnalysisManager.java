/**
 * 
 * @author Mario Alemi
 * @created   Mar 6, 2010
 * @version 0.1
 */
package pmanager;

import java.io.FileWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import org.strabil.manager.AnalysisManager;
import org.strabil.market.Agent;
import org.strabil.market.AgentSet;
import org.strabil.utils.DoTest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.XStream;

import pmarket.LoyaltyLevel;
import pmarket.OutputLibrary;
import pmarket.ProductStock;
import static pmarket.OutputLibrary.*;

public class pAnalysisManager implements AnalysisManager { 
	
                    	//ooooooOOOOOO000 000OOOOOOoooooo\\
	//ooooooOOOOOO000 Serialization and de-serialization 000OOOOOOoooooo\\

	public String serializeAgentSets(ArrayList<LoyaltyLevel>  levels, OutputLibrary protocol){
		if(protocol == XStream)
			return this.toXML(levels);
		else if(protocol == GSON)
			return this.toGSON(levels);
		else if(protocol == TEST)
			return this.toTEST(levels);
		else 
			return "CustomerFactory.flushAgents: protocol "+protocol+" not recognized. Only XML and JSON at the moment.";

	}

	/**
	 * @param serialString
	 * @param protocol
	 */
	@SuppressWarnings("unchecked")
	public Collection<AgentSet> deserializeLoyaltyLevel(String serialString, ArrayList<LoyaltyLevel> levels, OutputLibrary protocol){
		
		if(protocol == OutputLibrary.XStream)
			return (Collection<AgentSet>) this.fromXML(serialString);
		else if(protocol == OutputLibrary.GSON)
			return (Collection<AgentSet>) this.fromGSON(serialString, LoyaltyLevel.class);
		else{
			DoTest.warn( "CustomerFactory.flushAgents: protocol "+protocol+" not recognized. Only XML and JSON at the moment.");
			return null;
		}
	}
	

	private String toXML(Object obj){
		XStream xs = new XStream();
		xs.processAnnotations(LoyaltyLevel.class);

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
		xs.processAnnotations(LoyaltyLevel.class);

		return xs.fromXML(serialString);

	}

	private String toGSON(Object obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		Type collectionType;
		
		//This looks quite dirty to me...		
		if(obj.getClass() == (new ArrayList<LoyaltyLevel>()).getClass()){
			collectionType = new TypeToken<ArrayList<LoyaltyLevel>>(){}.getType();
		} else if(obj.getClass() == (new ArrayList<ProductStock>()).getClass()) {
			collectionType = new TypeToken<ArrayList<ProductStock>>(){}.getType();
		} else {
			DoTest.error("Dont know how to put in GSON "+obj.getClass());
			collectionType = null;
		}

		/*try {
			FileOutputStream fs = new FileOutputStream("ProductStock.json");
			PrintStream p = new PrintStream(fs);
			p.println(gson.toJson(this.levels));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 */
		return gson.toJson(obj, collectionType);

	}

	
	@SuppressWarnings("unchecked")
	private Object fromGSON(String serialString, Class type){
		//http://sites.google.com/site/gson/gson-user-guide
		//DOES NOT WORK

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if(type == LoyaltyLevel.class){
			Type collectionType = new TypeToken<ArrayList<LoyaltyLevel>>(){}.getType();
			return gson.fromJson(serialString, collectionType );

		} else if(type == ProductStock.class){
			Type collectionType = new TypeToken<ArrayList<ProductStock>>(){}.getType();
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
			e.printStackTrace();
		}
		 */

	}

	private String toTEST(Object obj){
		String xml = "TEST....... marshall";


		// Create a File to marshal to
		try {
			@SuppressWarnings("unused")
			FileWriter writer = new FileWriter("book.xml");
			//Marshaller.marshal(this.levels, writer); //Needs castor
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return xml;
	}

	//ooooooOOOOOO000 000OOOOOOoooooo\\
	//ooooooOOOOOO000 000OOOOOOoooooo\\

	public String printAgentSets(Collection<LoyaltyLevel> all, String separator){
		String o = "";
		for(LoyaltyLevel ll: all){
			o = o+ ll.getProgramName()+separator+ll.getPeriod()+separator+ll.getNumberAgents()+separator;
			for(int b=0; b<Agent.nBudgets; b++){
			//	DoTest.debug("BBB+++++++ "+b);
				o=o+   Math.round(ll.getAveBudget(b).getValue())+separator;
			}
			o= o+"\n"; 
		}
		return o;
	}

	
}
