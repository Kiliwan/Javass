package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class Meld {
	
	// static attribute containing all the possible melds
	public static List<Meld> ALL_MELDS = computeAll();
	
	/**
	 * Method to compute and output all possible melds
	 * @return		Immutable list of all possible melds
	 */
    private static List<Meld> computeAll() {
        List<Meld> all = new ArrayList<>();
        addAllQuartetsInto(all);
        addAllSuitsInto(all);
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns the points for a quartet of the given rank
     * @param rank		rank of the quartet
     * @return			number of points of the quartet
     */
    private static int quartetPoints(Rank rank) {
        switch (rank) {
        case NINE:
            return 150;
        case JACK:
            return 200;
        case TEN:
        case QUEEN:
        case KING:
        case ACE:
            return 100;
        default:
            throw new Error();
        }
    }

    /**
     * Compute and add all the possible quartets to the given list of melds
     * @param melds		list to add the quartets into
     */
    private static void addAllQuartetsInto(List<Meld> melds) {
    	List<Rank> ranksFrom9 = Rank.ALL.subList(Rank.NINE.ordinal(), Rank.COUNT);
    	for (Rank rank: ranksFrom9) {
    		Set<Card> quartet = new HashSet<>();
    		for (Color color: Color.ALL)
    			quartet.add(Card.of(color, rank));
    		melds.add(new Meld(quartet, quartetPoints(rank)));
    	}
    }

    /**
     * Returns the points for a suit of cards of the given length
     * @param size		size of the suit of cards
     * @return			number of points of the suit
     */
    private static int suitPoints(int size) {
        switch (size) {
        case 3: return 20;
        case 4: return 50;
        case 5: return 100;
        default: throw new Error();
        }
    }

    /**
     * Compute and add all the possible suits to the given suit of melds
     * @param melds		list to add the suits into
     */
    private static void addAllSuitsInto(List<Meld> melds) {
    	for (Color color: Color.ALL) {
    		for (int size = 3; size <= 5; size += 1) {
    			List<Card> cards = new ArrayList<>();
    			for(Rank rank : Rank.ALL) {
    				cards.add(Card.of(color, rank));
    			}
    			for (int i1 = 0, i2 = size;
    					i2 <= cards.size();
    					i1 += 1, i2 += 1) {
    				Set<Card> suit = new HashSet<>(cards.subList(i1,i2));
    				melds.add(new Meld(suit, suitPoints(size)));
    			}
    		}
    	}
    }

    /**
     * Returns all the melds possible with the given hand
     * @param hand		given collection of cards representing the hand
     * @return			list of melds contained in the given hand
     */
    public static List<Meld> allIn(Collection<Card> hand) {
    	List<Meld> allIn = new ArrayList<>();
    	for (Meld m: ALL_MELDS) {
    		if (hand.containsAll(m.cards())) {
    			allIn.add(m);
    		}
    	}
    	return allIn;
    }

    private final Set<Card> cards;
    private final int points;

    /**
     * Constructor of a meld
     * @param cards		set of cards of the meld
     * @param points	points of the meld
     */
    private Meld(Set<Card> cards, int points) {
        if (! (0 < points))
            throw new IllegalArgumentException("invalid points: " + points);

        this.cards = Collections.unmodifiableSet(cards);
        this.points = points;
    }

    /**
     * Getter for the list of cards of the meld
     */
    public Set<Card> cards() {
        return cards;
    }

    /**
     * Getter for the points of the meld
     */
    public int points() {
        return points;
    }

    @Override
    public String toString() {
        return String.format("%3d: %s", points, cards);
    }
}
