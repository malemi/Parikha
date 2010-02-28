/**
 * 
 */

/**
 * @author 3H
 *
 */

package Parikha;

import java.io.IOException;
import manager.*;
import pmanager.*;

public class Parikha {

	/**
	 * @param args command line options
	 * @throws IOException 
	 */
	public static void main(String[] args){

		RunManager rm = RunManager.getInstance();

		pRunAction pra= new  pRunAction();
		pEventAction pea=new pEventAction();

		int nEvent=1;
		int nPeriods = 1;
		rm.Initialize(pra, pea, nPeriods, "/Users/m/neo4j_db"); 
		//inputting the number of events we want
		try {
			rm.RunSimulation(nEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
