package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class PackedTrick {
	private PackedTrick() {}
	
	public static final int INVALID = 0xFFFFFFFF;
	
	private static final int INDEX_POS = 24;
	private static final int TRICK_SIZE = 4;
	private static final int CARD_SIZE = 6;
	private static final int PLAYER_SIZE = 2; 
	private static final int CARD_NUMBER = 4;
	private static final int COLOR_CARD_SIZE = 2;
	private static final int COLOR_SIZE = 16;

	
	/**
	 * Checks if given "packed" trick integer is valid
	 * @param pkTrick	given "packed" trick
	 * @return			true if valid, false otherwise
	 */
	public static boolean isValid(int pkTrick) {
		
		int index = Bits32.extract(pkTrick, INDEX_POS, TRICK_SIZE);

		if(pkTrick == INVALID || !(index >=0 && index < Jass.TRICKS_PER_TURN)) {
			return false;
		}
	
		int[] cards = new int[TRICK_SIZE];
		
		for(int i=0;i<TRICK_SIZE;i++) {
			cards[i] = Bits32.extract(pkTrick, CARD_SIZE * i, CARD_SIZE);
		}
		for(int j = 0; j < cards.length -1; j++) {
				if(PackedCard.isValid(cards[j+1]) && !PackedCard.isValid(cards[j])) {
					return false;
				}
		}
		return true;
		
	}
	
	/**
	 * Returns a "packed" trick integer representing an empty trick
	 * @param trump			trump of the turn
	 * @param firstPlayer	first player of the turn
	 */
	public static int firstEmpty(Color trump, PlayerId firstPlayer) {
		
		return Bits32.pack(PackedCard.INVALID, CARD_SIZE, 
				           PackedCard.INVALID, CARD_SIZE, 
				           PackedCard.INVALID, CARD_SIZE, 
						   PackedCard.INVALID, CARD_SIZE, 
						   0, TRICK_SIZE, 
						   firstPlayer.ordinal(), PLAYER_SIZE, 
						   trump.ordinal(), COLOR_CARD_SIZE);
	}
	
	/**
	 * Returns a "packed" trick integer representing the next empty trick after a finished one in the same turn
	 * @param pkTrick		"packed" trick integer of the last trick
	 */
	public static int nextEmpty(int pkTrick) {
		assert(isValid(pkTrick));
		
		int index = PackedTrick.index(pkTrick);
		int trump = PackedTrick.trump(pkTrick).ordinal();                                                           
		int winningPlayer = winningPlayer(pkTrick).ordinal();
		
		if(isLast(pkTrick)) { // - 1 because index starts at 0 in PackedTrick
			return INVALID;
		} else {
			return Bits32.pack(PackedCard.INVALID, CARD_SIZE, 
					           PackedCard.INVALID, CARD_SIZE, 
					           PackedCard.INVALID, CARD_SIZE, 
					           PackedCard.INVALID, CARD_SIZE, 
					           index + 1, TRICK_SIZE, 
					           winningPlayer, PLAYER_SIZE, 
					           trump, COLOR_CARD_SIZE);
		}
	}
	
	/**
	 * Checks if given trick is the last of the turn
	 * @param pkTrick		"packed" trick
	 * @return				true if last, false otherwise
	 */
	public static boolean isLast(int pkTrick) {
		assert(isValid(pkTrick));
		
		return PackedTrick.index(pkTrick) == Jass.TRICKS_PER_TURN - 1; // - 1 because index starts at 0 in PackedTrick
	}
	
	/**
	 * Checks if given trick is empty (no cards played)
	 * @param pkTrick		"packed" trick
	 * @return				true if last, false otherwise
	 */
	public static boolean isEmpty(int pkTrick) {
		assert(isValid(pkTrick));
		
		return PackedTrick.size(pkTrick) == 0;
	}
	
	/**
	 * Checks if given trick is full (all cards played)
	 * @param pkTrick		"packed" trick
	 * @return				true if full, false otherwise
	 */
	public static boolean isFull(int pkTrick) {
		assert(isValid(pkTrick));
		
		return PackedTrick.size(pkTrick) == CARD_NUMBER ;
		
	}
	/**
	 * Checks the current size of the given trick, how many cards have been played
	 * @param pkTrick		"packed" trick
	 * @return				integer representing the number of cards played
	 */
	public static int size(int pkTrick) {
		assert(isValid(pkTrick));
		
		int count = 0;

		for(int i = 0; i < CARD_NUMBER; ++i) {
			int toAdd = PackedTrick.card(pkTrick, i);
			if(toAdd != PackedCard.INVALID) {
				++count;
			}
		}

		return count;
	}
	
	
	/**
	 * Returns the card color that is trump in the trick
	 * @param pkTrick		"packed" trick
	 */
	public static Color trump(int pkTrick) {
		assert(isValid(pkTrick));
		
		int trumpPos = 30;
		return Card.Color.ALL.get((Bits32.extract(pkTrick, trumpPos, COLOR_CARD_SIZE)));
	}
	
	/**
	 * Returns the n-th player, corresponding to the index, after the first player of the trick
	 * @param pkTrick		"packed" trick
	 * @param index			index of the player to get
	 */
	public static PlayerId player(int pkTrick, int index) {
		assert(isValid(pkTrick));
		
		int playerPos = 28;
		int player = Bits32.extract(pkTrick, playerPos, PLAYER_SIZE);
		
		return PlayerId.ALL.get((player + index) % PlayerId.COUNT);
	}
	
	/**
	 * Returns the index of the trick
	 * @param pkTrick		"packed" trick
	 */
	public static int index(int pkTrick) {
		assert(isValid(pkTrick));
		
		return Bits32.extract(pkTrick, INDEX_POS, TRICK_SIZE);
	}
	
	/**
	 * Returns the n-th, corresponding to the index, card of the trick
	 * @param pkTrick		"packed" trick
	 * @param index			index of the card to get
	 */
	public static int card(int pkTrick, int index) {
		assert(isValid(pkTrick));
		
		return Bits32.extract(pkTrick, index * CARD_SIZE, CARD_SIZE);
	}
	
	/**
	 * Add the given card to the given trick
	 * @param pkTrick		"packed" trick
	 * @param pkCard		"packed" card
	 * @return				new "packed" trick with the given card added
	 */
	public static int withAddedCard(int pkTrick, int pkCard) {
		assert(isValid(pkTrick) && PackedCard.isValid(pkCard));

		int i = 0;
		while(PackedTrick.card(pkTrick, i) != PackedCard.INVALID) {
			i = (i + 1) % CARD_NUMBER;
		}
		return replace(pkTrick, pkCard,i);
	}
	
	/**
	 * Replace the card in the trick at the given index by the given card
	 * @param pkTrick		"packed" trick
	 * @param pkCard		"packed" card
	 * @param index			index of the card to replace
	 * @return				new "packed" trick with the card replaced
	 */
	private static int replace(int pkTrick, int pkCard, int index) {
		
		pkTrick &= ~Bits32.mask(index * CARD_SIZE, CARD_SIZE);
		return pkTrick | (pkCard << index * CARD_SIZE);
	}
	
	/**
	 * Returns the base color (starting color) of the trick
	 * @param pkTrick		"packed" trick
	 */
	public static Color baseColor(int pkTrick) {
		assert(isValid(pkTrick));
		
		return PackedCard.color(PackedTrick.card(pkTrick, 0));
	}
	
	/**
	 * Checks the which cards of the given hand can be played in the given trick
	 * @param pkTrick		"packed" trick
	 * @param pkHand		"packed" card set
	 * @return				new card set containing only cards of the hand that can be played
	 */
	public static long playableCards(int pkTrick, long pkHand) {
		assert(isValid(pkTrick) && 
			  PackedCardSet.isValid(pkHand));
		
		int card1 = PackedTrick.card(pkTrick, 0);
		Color trump = PackedTrick.trump(pkTrick);
		long trumpToPlay = 0L;
		long colorToPlay = 0L;
		int jackOrd = Card.Rank.JACK.ordinal();

		
		if(card1 == PackedCard.INVALID) { // if first card to play, can play whatever
			return pkHand;
		} 
		
		Color turnColor = PackedCard.color(card1);
		long trumpHand = PackedCardSet.subsetOfColor(pkHand, trump);
		long colorHand = PackedCardSet.subsetOfColor(pkHand, turnColor);
		
		// trump cards selection
		
		if(trumpHand == 0 || trumpHand == (1L << (COLOR_SIZE * trump.ordinal() + jackOrd))) {
			// if no trump or only jack, can play whatever
			trumpToPlay = trumpHand;			
		} else {
			// if trump, only stronger than actual strongest	
			for(int i = 0; i < PackedCardSet.size(trumpHand);i++) {
				if(PackedCard.isBetter(trump, PackedCardSet.get(trumpHand, i), getStrongestCardInTrick(pkTrick))) {
					trumpToPlay = PackedCardSet.add(trumpToPlay, PackedCardSet.get(trumpHand, i));
				}
			}
		}
		
		// same color cards selection
		
		if(turnColor.equals(trump)) {
			// if color same as trump
			if(trumpHand == 0 || trumpHand == (1L << (COLOR_SIZE * trump.ordinal() + jackOrd))) {
				// if no trump or only jack can play any
				colorToPlay = PackedCardSet.difference(pkHand, trumpHand);
			} else {
				// else can play all trump
				trumpToPlay = trumpHand;
			}
			
		} else {
			
			if(colorHand == 0){ 
				// if no card of asked color
				colorToPlay = PackedCardSet.difference(pkHand, trumpHand);
			} else { 
				// if card of asked color, only stronger
				colorToPlay = PackedCardSet.subsetOfColor(pkHand, turnColor);
			}
		}
		
		if(colorToPlay == 0 && trumpToPlay == 0) { // obligated to play trump under
			trumpToPlay = trumpHand;
		}
		
		// combining trump and same color cards selection
		return trumpToPlay | colorToPlay;
	}
	
	/**
	 * Find the strongest card in the given packed trick
	 * @param pkTrick		"packed" trick
	 * @return winningCard  strongest Card
	 */
	private static int getStrongestCardInTrick(int pkTrick) {
		assert(isValid(pkTrick));
		
		Color trump = PackedTrick.trump(pkTrick);
		List<Integer> list = new ArrayList<>();
		for(int i = 0; i < CARD_NUMBER; ++i) {
			int toAdd = PackedTrick.card(pkTrick, i);
			list.add(toAdd);
		}
		
		int winningCard = list.get(0);
		for(int i : list) {
			if(i != PackedCard.INVALID && PackedCard.isBetter(trump, i, winningCard)) {
				winningCard = i;
			}
		}
		return winningCard;
	}
	
	/**
	 * Returns the total points of the cards played in the trick
	 * @param pkTrick		"packed" trick
	 */
	public static int points(int pkTrick) {
		assert(isValid(pkTrick));
		
		Color trump = PackedTrick.trump(pkTrick);
		int points = 0;

		for(int i = 0; i < PackedTrick.size(pkTrick); ++i) {
			int card = PackedTrick.card(pkTrick, i);
			points += PackedCard.points(trump, card);
		}
		
		if(PackedTrick.isLast(pkTrick)) {
			points += Jass.LAST_TRICK_ADDITIONAL_POINTS; // "5 de der"
		} 
		return points;
	}
	
	/**
	 * Returns the player that won the trick
	 * @param pkTrick		"packed" trick
	 * @return				PlayerId of the player who won
	 */
	public static PlayerId winningPlayer(int pkTrick) {
		assert(isValid(pkTrick));
		
		int win = PackedTrick.getStrongestCardInTrick(pkTrick);
		int firstPlayer = PackedTrick.player(pkTrick, 0).ordinal();
		
		List<Integer> list = new ArrayList<>();
		for(int i = 0; i < 4; ++i) {
			int toAdd = PackedTrick.card(pkTrick, i);
			list.add(toAdd);
		}
		
		return PlayerId.ALL.get((list.indexOf(win) + firstPlayer) % PlayerId.COUNT);
	}
	
	/**
	 * Returns printable String representing the trick, formated as : Turn number, first card, second card, third card, fourth card
	 * @param pkTrick		"packed" trick
	 */
	public static String toString(int pkTrick) {
		
		if(pkTrick == PackedTrick.INVALID) {
			return "Trick is invalid";
		}
        StringJoiner j = new StringJoiner("/", "[", "]");
        j.add("Turn : " + (index(pkTrick) + 1) + "  ");
        for(int i = 0; i < CARD_NUMBER; ++i) {
        	j.add(PackedCard.toString(PackedTrick.card(pkTrick, i)));
        }
		return j.toString();
	}
}
