package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits64;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class PackedCardSet {
    
    private PackedCardSet(){}
    
    public static final long EMPTY = 0L;
    public static final long ALL_CARDS = 0x1FF01FF01FF01FFL;
    
	private static final int COLOR_SIZE = 16;
	private static final Map<Color, Long> subsets = doSubsets();
	private static final Map<Integer, Long> above = doAbove();

	
    
    /**
     * Checks to see if given value corresponds to a correct "packed" card set value
     * @param pkCardSet		"packed" card set value
     * @return				true if valid, false otherwise
     */
    public static boolean isValid(long pkCardSet) {
    	
        if(pkCardSet < EMPTY || pkCardSet > ALL_CARDS ) {
            return false;
       } else {
    	   return (~ALL_CARDS & pkCardSet) == 0L;
        }
        
    }
    
    /** 
     * Returns a "packed" card set of all the card stronger than the one given in the color of trump
     * @param pkCard	"packed" card in the color of trump
     * @return			"packed" card set of stronger cards than the one given
     */
    public static long trumpAbove(int pkCard) {
        assert(PackedCard.isValid(pkCard));

        return above.get(pkCard);
    }
    
    private static Map<Integer, Long> doAbove() {
    	
    	Map<Integer, Long> map = new HashMap<>();
        CardSet all = CardSet.ofPacked(ALL_CARDS);
        
    	for(int i = 0; i < all.size(); ++i ) {
    		
    		Card toDo = all.get(i);
            long toReturn = EMPTY;

            for(int j = 0; j < Rank.COUNT; ++j) {
            	
            	Card c = Card.of(toDo.color(), Rank.ALL.get(j));
            	
            	if(c.isBetter(toDo.color(), toDo)) {
            		toReturn = toReturn | CardSet.of(Arrays.asList(c)).packed();
            	}
            }
            map.put(toDo.packed(), toReturn);
    	}
        return map;
    }
 
    /**
     * Returns a card set containing only the card corresponding to the given card
     * @param pkCard	"packed" integer of the given card
     * @return			"packed" card set containing only the given card
     */
    public static long singleton(int pkCard) {
        assert(PackedCard.isValid(pkCard));
        
        return 1L << pkCard;    
    }
    
    /**
     * Checks to see if the given card set is empty
     * @param pkCardSet		"packed" card set given
     * @return				true if empty, false otherwise
     */
    public static boolean isEmpty(long pkCardSet) {
        assert(isValid(pkCardSet));
        
        return pkCardSet == PackedCardSet.EMPTY;
    }
    
    /**
     * Returns the number of cards in the card set
     * @param pkCardSet		"packed" card set given
     * @return				number of cards present
     */
    public static int size(long pkCardSet) {
        assert(isValid(pkCardSet));
        
        return Long.bitCount(pkCardSet); // number of 1's
    }
    
    /**
     * Get the n-th card present in the given deck according to the given index
     * @param pkCardSet		"packed" card set given
     * @param index			n-th card of the deck wanted
     * @return				"packed" integer representing the card asked
     */
    public static int get(long pkCardSet, int index) {
    	assert(PackedCardSet.isValid(pkCardSet));
    	
    	long cardSet = pkCardSet;
    	for (int i=0; i < index; i++) {
    		cardSet = cardSet & ~Long.lowestOneBit(cardSet);
    	}
    	
		return Long.numberOfTrailingZeros(cardSet);
    }
    
    /**
     * Add a card to the card set, if already there doesn't change anything
     * @param pkCardSet		"packed" card set
     * @param pkCard		"packed" card to add to the set
     * @return				new "packed" card set with given card added
     */
    public static long add(long pkCardSet, int pkCard) {
        assert(isValid(pkCardSet) && 
        	   PackedCard.isValid(pkCard));
        
        long toChange = 1L << pkCard;
        
        return pkCardSet | toChange; 
    }
    
    /**
     * Remove a card from the card set, if already not there doesn't change anything
     * @param pkCardSet		"packed" card set
     * @param pkCard		"packed" card to remove from the set
     * @return				new "packed" card set with given card removed
     */
    public static long remove(long pkCardSet, int pkCard) {
        assert(isValid(pkCardSet) && 
        	   PackedCard.isValid(pkCard));
        
        long toChange = ALL_CARDS ^(1L << pkCard);
        
        return pkCardSet & toChange;
    }
    
    /**
     * Check if card set contains given card
     * @param pkCardSet		"packed" card set
     * @param pkCard		"packed" card
     * @return				true if card in set, false otherwise
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        assert(isValid(pkCardSet) && 
        	   PackedCard.isValid(pkCard));
        
        long rightBit = Bits64.extract(pkCardSet, pkCard, 1);
        
        return rightBit == 1L;    
    }
    
    /**
     * Returns complement (inverse) of the card set, missing cards of the given card set
     * @param pkCardSet		"packed" card set
     * @return				"packed" complement of the given card set
     */
    public static long complement(long pkCardSet) {
    	assert(isValid(pkCardSet));
        
        return ALL_CARDS & ~pkCardSet;    
    }
    
    /**
     * Returns new card set containing cards of both given card sets
     * @param pkCardSet1		first "packed" set given
     * @param pkCardSet2		second "packed" set given
     * @return					"packed" card set of the union of the two
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
    	assert(isValid(pkCardSet1) && 
    		   isValid(pkCardSet2));
        
        return pkCardSet1 | pkCardSet2;    
    }
    
    /**
     * Returns new card set containing only cards present in both card sets
     * @param pkCardSet1		first "packed" set given
     * @param pkCardSet2		second "packed" set given
     * @return					"packed" card set of the intersection of the two
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
    	assert(isValid(pkCardSet1) && 
    		   isValid(pkCardSet2));
        
        return pkCardSet1 & pkCardSet2;
    }
    
    /**
     * Returns new card set containing the cards of the first set given minus the cards of the second
     * @param pkCardSet1		first "packed" set given
     * @param pkCardSet2		second "packed" set given
     * @return					"packed" card set of the difference of the two
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
    	assert(isValid(pkCardSet1) && 
    		   isValid(pkCardSet2));

        long differencesBoth = complement(pkCardSet2);
        
        return intersection(pkCardSet1, differencesBoth);
    }
    
    /**
     * Returns only the portion of a card set corresponding to the given color
     * @param pkCardSet		"packed" card set given
     * @param color			color given
     * @return				"packed" card set containing only the cards of the given color
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
    	assert(isValid(pkCardSet));

    	return subsets.get(color) & pkCardSet;
    }
    
    private static Map<Color, Long> doSubsets(){
    	
    	Map<Color, Long> map = new HashMap<>();
    	for(int i = 0; i < Color.COUNT; ++i) {
    		map.put(Color.ALL.get(i), Bits64.extract(ALL_CARDS, i * COLOR_SIZE, COLOR_SIZE) << i * COLOR_SIZE);
    	}
    	return map;	
    }
    
    /**
     * Returns a String with the cards of the given card set in the format {color1},{color2},... with color first then the rank 
     * @param pkCardSet		"packed" card set given
     * @return				String with all the card of the card set
     */
    public static String toString(long pkCardSet) {
    	assert(isValid(pkCardSet));

        StringJoiner j = new StringJoiner(",", "{", "}");
        
        for(int i = 0; i < PackedCardSet.size(pkCardSet); ++i) {
        	
            int card = PackedCardSet.get(pkCardSet, i);
            if(card != 0) {
                j.add(PackedCard.toString(card));
            }
        }
        
        return j.toString();
    }

}
