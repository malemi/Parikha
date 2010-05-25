/**
 * @author 3H
 *
 */

package Parikha;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

import org.strabil.manager.RunManager;

import pmanager.*;

public class Parikha {

	/**
	 * @param args command line options
	 * @throws IOException .
	 * @throws InvalidPropertiesFormatException . 
	 */
	public static void main(String[] args) throws InvalidPropertiesFormatException, IOException{

		RunManager rm = RunManager.getInstance();

		pRunAction pra= new  pRunAction();
		pEventAction pea=new pEventAction();

		rm.readConfig("config.xml");
		int nEvent=  Integer.parseInt( rm.getProperty("NumberEvents")); 
		int nPeriods = Integer.parseInt( rm.getProperty("NumberPeriods"));
		
		rm.Initialize(pra, pea, nPeriods, "/Users/m/neo4j_db"); 

		rm.readConfig("config.xml");
		
		//inputting the number of events we want
		try {
			rm.RunSimulation(nEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
