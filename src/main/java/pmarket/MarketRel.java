package pmarket;

import org.neo4j.graphdb.Relationship;
import org.strabil.market.WeightedRelationship;

public class MarketRel extends WeightedRelationship {

	public MarketRel(Relationship rel) {
		super(rel);
	}

	private Relationship rel;
	private static final String KEY_LOYALTY = "LoyaltyLevel";

	public Relationship getUnderlyingRelationship() {
		return rel;
	}

	public void setLoyaltyLevel(String loyalty){

		rel.setProperty(KEY_LOYALTY, loyalty);
	}


}
