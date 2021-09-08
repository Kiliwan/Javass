package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public final class MeldSet {
	
	  private final Set<Meld> melds;

	  /**
	   * Method to test if the given collection of melds is mutually disjoint to each other, if they do not contain the same cards
	   * @param melds		given collection of melds
	   * @return			true if mutually disjoint, false otherwise
	   */
	  public static boolean mutuallyDisjoint(Collection<Meld> melds) {
		  List<Set<Card>> allSetsOfCards = new ArrayList<>();
		  for (Meld m: melds) {
			  allSetsOfCards.add(m.cards());
		  }
		  return Sets.mutuallyDisjoint(allSetsOfCards);
	  }
	  
	  /**
	   * Returns all possible mutually disjoint meld sets from the given hand
	   * @param hand	collection of cards representing the hand
	   * @return		list of mutually disjoint meld sets of the given hand
	   */
	  public static List<MeldSet> allIn(Collection<Card> hand) {
		  List<MeldSet> r = new ArrayList<>();
		  for (Set<Meld> melds: Sets.powerSet(Meld.allIn(hand))) {
			  if (mutuallyDisjoint(melds)) {
				  r.add(new MeldSet(melds));
			  }
		  }
		  return r;
	  }
	  
	  /**
	   * Returns the best meld set possible from a given collection of cards using the MeldSetByPointsComparator
	   * @param hand	given collection of cards representing the hand
	   * @return		best possible meld set from the given hand
	   */
	  public static MeldSet getBestMeldSet(Collection<Card> hand) {
		  List<MeldSet> melds = MeldSet.allIn(hand);
		  melds.sort(new MeldSetByPointsComparator());
		  return melds.get(melds.size()-1);
	  }

	  /**
	   * Method to crate a meld set from a collection of cards
	   * @param melds		collection of cards of the meld set
	   * @return			new MeldSet instance representing the given collection of cards
	   */
	  public static MeldSet of(Collection<Meld> melds) {
	    if (!mutuallyDisjoint(melds))
	      throw new IllegalArgumentException();
	    return new MeldSet(melds);
	  }

	  /**
	   * Constructor for a MeldSet from a collection of melds
	   * @param melds	collection of melds of the MeldSet
	   */
	  private MeldSet(Collection<Meld> melds) {
	    this.melds = Collections.unmodifiableSet(new HashSet<>(melds));
	  }
	  
	  /**
	   * Returns an immutable list of all the melds in the meld set
	   */
	  public Set<Meld> getMelds(){
		  return Collections.unmodifiableSet(melds);
	  }

	  /**
	   * Returns the total points of all the melds in the meld set
	   */
	  public int points() {
	    int points = 0;
	    for (Meld m: melds)
	      points += m.points();
	    return points;
	  }

	  @Override
	  public String toString() {
	    StringJoiner s = new StringJoiner("\n", "", "");
	    for (Meld m: melds)
	      s.add(m.cards().toString());
	    if(s.length() == 0)
	    	s.add("No melds");
	    return String.format("%3d points \n %s", points(), s);
	  }
}

/**
 * Comparator used to compare the meld sets to each other
 * Compares the meld sets in regard of the respective points of each
 */
class MeldSetByPointsComparator implements Comparator<MeldSet> {
	@Override
	public int compare(MeldSet m1, MeldSet m2) {
		return Integer.compare(m1.points(), m2.points());
	}
}