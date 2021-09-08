package ch.epfl.javass.jass;


import static ch.epfl.javass.jass.PackedCard.pack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class Card {

    private final int nbCard ;

    /**
     * Constructor of Card, use pack() to assign to the integer attribute a number representing the card
     * @param c		color of card
     * @param r		rank of card
     */
    private Card(Color c, Rank r) {
        this.nbCard =  pack(c,r);
    }

    /**
     * Create new card of color and rank given
     * @param c		color of card
     * @param r		rank of card
     * @return		card of color and rank given, using regular constructor
     */
    public static Card of(Color c, Rank r) {
        return new Card(c, r);
    }

    /**
     * Create new card represented by "packed" integer given
     * @param packed	"packed" integer of card
     * @return			card of color and rank given in "packed" integer, using regular constructor
     */
    public static Card ofPacked(int packed) {
    	
    	//verifying the integer is done in the "packed" version of the method
        Color c = PackedCard.color(packed);
        Rank r = PackedCard.rank(packed);
        return of(c,r);
    }

    /**
     * Returns "packed" integer of the card
     */
    public int packed() {
        return this.nbCard;
    }

    /**
     * Returns color of the card
     */
    public Color color(){
        return PackedCard.color(this.nbCard);
    }

    /**
     * Returns rank of the card
     */
    public Rank rank(){
        return PackedCard.rank(this.nbCard);
    }

    /**
     * Check if card is better than other card given knowing the trump color of the game
     * @param trump		trump color of the game
     * @param that		other card to which it will be compared
     * @return			true if it is better than the other, false otherwise
     */
    public boolean isBetter(Color trump, Card that) {
    	
    	//verifying the card is already done in the "packed" version of the method
        return PackedCard.isBetter(trump, this.nbCard, that.packed());
    }

    /**
     * Counts points of card knowing the trump color of the game
     * @param trump		trump color of the game
     * @return			points of the card
     */
    public int points(Color trump) {
        return PackedCard.points(trump, this.nbCard);
    }

    @Override
    public boolean equals(Object that0) {
    	
    	if(!(that0 instanceof Card)) {
            return false;
        }
        return this.nbCard == ((Card)that0).packed();  

    }

    @Override
    public int hashCode() {
        return this.nbCard; // returns instead the "packed" integer
    }

    @Override
    public String toString() {
        return color().toString() + rank().toString();
    }

    /**
     * Enumeration used to indicate color of the card
     * Each color is given a symbol, which it will print as representation of the color
     */
    public enum Color {
        SPADE ("\u2660"),
        HEART ("\u2665"),
        DIAMOND ("\u2666"),
        CLUB ("\u2663");
        
    	// Unicode symbols, filled
        public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));
        public static final int COUNT = ALL.size();

        private final String symbole;
        private Color(String symbole){
            this.symbole = symbole;
        }

        @Override
        public String toString() {
            return symbole;
        }
    }

    /**
     * Enumeration used to indicate rank of the card
     * Each rank is given a character, which it will print as representation of the rank
     */
    public enum Rank {
        SIX ("6",0),
        SEVEN ("7",1),
        EIGHT ("8",2),
        NINE ("9",7),
        TEN ("10",3),
        JACK ("J",8),
        QUEEN ("Q",4),
        KING ("K",5),
        ACE ("A",6);

    	// power scale of regular cards taken from position in enumeration (and so in list)
        public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(values()));
        public static final int COUNT = ALL.size();

        private final String rang;
        private final int trumpOrdinal;
        private Rank(String rang, int trumpOrdinal){
            this.rang = rang;
            this.trumpOrdinal = trumpOrdinal;
        }

        /**
         * Power scale to use when playing with trump color
         * @return 		position on the trump power scale of the card
         */
        public int trumpOrdinal() {
            return trumpOrdinal;
        }

        @Override
        public String toString() {
            return rang;
        }
    }
}