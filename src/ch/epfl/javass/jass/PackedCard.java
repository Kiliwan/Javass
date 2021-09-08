package ch.epfl.javass.jass;
 
import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class PackedCard {
    
    public static int INVALID = 0b111111;
    
    private final static int RANK_SIZE = 4;
	private final static int COLOR_CARD_SIZE = 2;

	private PackedCard() {}
    
    /**
     * Determine if the given "packed" card integer really represents a playing card
     * @param pkCard	"packed" card integer
     * @return			true if valid, false otherwise
     */
    public static boolean isValid(int pkCard) {
        if(pkCard < 0 || pkCard > INVALID) {
            return false;
        }

        // no need to check for color as it is only 2 bits so always under 4
        
        int rank = Bits32.extract(pkCard, 0, RANK_SIZE);
        
        boolean rankValid = rank < Card.Rank.COUNT;
        
        return rankValid;
    }
    
    /**
     * Pack the color and rank information into an integer representing a playing card
     * @param c		color of card
     * @param r		rank of card
     * @return		integer representing "packed" playing card
     */
    public static int pack(Card.Color c, Card.Rank r) {
        int color = c.ordinal();
        int rank = r.ordinal();
        return rank | (color << RANK_SIZE) ;
    }

    /**
     * Get the color of a playing card from its "packed" integer
     * @param pkCard	"packed" integer
     * @return			color (from list of all colors)
     */
    public static Card.Color color(int pkCard) {
        assert(isValid(pkCard));
        
        int color = Bits32.extract(pkCard, RANK_SIZE, COLOR_CARD_SIZE); //extract number corresponding to the color
        return Card.Color.ALL.get(color);
    }
    
    /**
     * Get the rank of a playing card from its "packed" integer
     * @param pkCard	"packed" integer
     * @return			rank (from list of all ranks)
     */
    public static Card.Rank rank(int pkCard) {
        assert(isValid(pkCard));
        
        int rank = Bits32.extract(pkCard, 0, RANK_SIZE); // extract number corresponding to the rank
        return Card.Rank.ALL.get(rank);
    }

    /**
     * Check if first card given (L) is better than second card given (R) knowing the trump color of the actual game
     * @param trump		trump color of the actual game
     * @param pkCardL	first card
     * @param pkCardR	second card
     * @return			true if first card better, false otherwise
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert(isValid(pkCardL) && 
        	   isValid(pkCardR));
        
        Card.Color colorL = color(pkCardL);
        Card.Color colorR = color(pkCardR);
        Card.Rank rankL = rank(pkCardL);
        Card.Rank rankR = rank(pkCardR);
        

        if(colorL == colorR) {
            if(colorL == trump){
            	return rankL.trumpOrdinal() > rankR.trumpOrdinal(); // if both are trump, use trump power scale
            } else {
            	return rankL.ordinal() > rankR.ordinal(); // if both are same but not trump, use regular power scale
            }
        } else {
        	return colorL == trump; // if only first is trump, it wins anyway, if not comparable, return false
        }
    }

    /**
     * Counts the points of a particular card knowing the trump color of the actual trump
     * @param trump		trump color of the actual game
     * @param pkCard	"packed" integer representing card
     * @return			number of points given by the card
     */
    public static int points(Card.Color trump, int pkCard) {
        assert(isValid(pkCard));
        
        if(color(pkCard) == trump) {
            switch (rank(pkCard)) { // if trump, use trump points scale
                case NINE : return 14;

                case TEN : return 10;

                case JACK :  return 20;

                case QUEEN : return 3;

                case KING : return 4;

                case ACE : return 11;

                default : return 0;

            }

        } else {

            switch (rank(pkCard)) { // else, use regular points scale
                case TEN : return 10;

                case JACK :  return 2;

                case QUEEN : return 3;

                case KING : return 4;

                case ACE : return 11;

                default : return 0;

            }
        }
    }

    /**
     * Returns a String with the color (symbol) and rank of a card
     * @param pkCard	"packed" integer of the card
     * @return			String with the color first then the rank
     */
    public static String toString(int pkCard) {

        if(pkCard == PackedCard.INVALID) {
        	return ("None");
        }
        return color(pkCard).toString()+rank(pkCard).toString();
    }
}