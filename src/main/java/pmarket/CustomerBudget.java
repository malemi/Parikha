package pmarket;


/**
 * General Budget denomination: GOODS, SERVICES, LOYALTY_PRESENT, DIFFERENCE_UP/DOWN, SIX, SEVEN, EIGHT, NINE
 * 
 * @author Mario Alemi
 *
 */
public enum CustomerBudget {

	/**
	 * The annual expense of the customer per year
	 */
	GOODS, 
	/**
	 * The budget for the services
	 */
	SERVICES,
	/**
	 * Budget for services offered by the seller
	 */
	LOYALTY_PRESENT, 
	/**
	 * Difference with the sectore above (bigger customers)
	 */
	DIFFERENCE_UP,
	/**
	 * Difference with the sectore below (smaller customers)
	 */
	DIFFERENCE_DOWN,
	
	SIX, SEVEN, EIGHT, NINE
	
}
