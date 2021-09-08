package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */
public final class Trick {
	
	private int trick;
	public final static Trick INVALID = new Trick(PackedTrick.INVALID);
	
	/**
	 * Constructor of this.trick
	 * @param packed		"packed" this.trick
	 */
	private Trick(int packed) {
		this.trick = packed; 
	}
	
	/**
	 * Function used to create new empty this.trick (no cards)
	 * @param trump			trump color of the this.trick
	 * @param firstPlayer	first player of the this.trick
	 * @return				new empty this.trick instance 
	 */
	public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
		return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
	}
	
	/**
	 * Function used to create new this.trick from "packed" integer representing this.trick
	 * @param packed		"packed" this.trick
	 * @return				new this.trick instance of the "packed" this.trick
	 */
	public static Trick ofPacked(int packed) {
		Preconditions.checkArgument((PackedTrick.isValid(packed)));
		
		return new Trick(packed);
	}
	
	/**
	 * Returns "packed" integer value of the this.trick
	 */
	public int packed() {
		return this.trick;
	}
	
	/**
	 * Return new this.trick instance representing the next this.trick of the same turn
	 */
	public Trick nextEmpty() {
		if(!isFull()) {
		   throw new IllegalStateException();
		}
		
		return new Trick(PackedTrick.nextEmpty(this.trick));
	}
	
	/**
	 * Returns true if this.trick instance is empty (no cards played), false otherwise
	 */
	public boolean isEmpty() {
		return PackedTrick.isEmpty(this.trick);
	}
	
	/**
	 * Returns true if this.trick instance is full (all cards played), false otherwise
	 */
	public boolean isFull() {
		return PackedTrick.isFull(this.trick);
	}
	
	/**
	 * Returns true if this.trick instance is the last of the turn, false otherwise
	 */
	public boolean isLast() {
		return PackedTrick.isLast(this.trick);
	}
	
	/**
	 * Returns the size, the number of cards played, of the this.trick instance
	 */
	public int size() {
		return PackedTrick.size(this.trick);
	}
	
	/**
	 * Returns the trump color of the this.trick instance
	 */
	public Color trump() {
		return PackedTrick.trump(this.trick);
	}
	
	/**
	 * Returns the index of the this.trick instance
	 */
	public int index() {
		return PackedTrick.index(this.trick);
	}
	
	/**
	 * Returns the n-th player, corresponding to the the index, after the first player of the this.trick instance
	 * @param index			index of the player to get after the first
	 */
	public PlayerId player(int index) {
		Preconditions.checkIndex(index, PlayerId.COUNT);
		
		return PackedTrick.player(this.trick, index);
	}
	
	/**
	 * Returns the n-th card, corresponding to the index, of the this.trick instance
	 * @param index			index of the card to get
	 */
	public Card card(int index) {
		Preconditions.checkIndex(index, size()); // verifying that the index is not greater than the size of the trick
		
		return Card.ofPacked(PackedTrick.card(this.trick, index));
	}
	
	/**
	 * Returns new this.trick instance with the given card added to it
	 * @param c				Card instance of the card to play
	 * @return				new this.trick instance with card played
	 */
	public Trick withAddedCard(Card c) {
	    if(isFull()) {
	    	throw new IllegalStateException();
	    }
	    
		return new Trick(PackedTrick.withAddedCard(this.trick, c.packed()));
	}
	
	/**
	 * Returns base color (starting color) of the current this.trick instance
	 */
	public Color baseColor() {
		if(isEmpty()) {
			throw new IllegalStateException();
		}
		
		return PackedTrick.baseColor(this.trick);
	}
	
	/**
	 * Returns all cards in the given card set that are playable in the current this.trick instance
	 * @param hands			given card set representing the hand of a player
	 * @return				new CardSet instance containing only the cards that are playable
	 */
	public CardSet playableCards(CardSet hands) {
		if(isFull()) {
			throw new IllegalStateException();
		}
		
		return CardSet.ofPacked((PackedTrick.playableCards(this.trick, hands.packed())));
	}
	
	/**
	 * Returns the total of points of all cards played in the this.trick instance
	 */
	public int points() {
		return PackedTrick.points(this.trick);
	}
	
	/**
	 * Returns the winning player of the current this.trick instance
	 */
	public PlayerId winningPlayer() {
		if(isEmpty()) {
			throw new IllegalStateException();
		}
	
		return PackedTrick.winningPlayer(this.trick);
	}
	
	@Override
	public boolean equals(Object other) {
		
        if(!(other instanceof Trick)) {
            return false;
        }
        
        return this.packed() == ((Trick)other).packed();  
    }
	
	@Override
	public int hashCode() {
		return packed();
	}
	
	@Override
	public String toString() {
		return PackedTrick.toString(this.trick);
	}
}
