package ch.epfl.javass.jass;

import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class CardSet {
    
    private final long cardSet; // "packed" CardSet long

    public static final CardSet EMPTY = new CardSet(PackedCardSet.EMPTY);
    public static final CardSet ALL_CARDS = new CardSet(PackedCardSet.ALL_CARDS);
    
    /**
     * Constructor of Score; throws Exception if given "packed" CardSet is invalid
     * @param packed		"packed" CardSet
     */
    private CardSet(long packed) {
    	this.cardSet = packed; 
    }
    
    /**
     * Method used to construct a new CardSet instance from a list of valid cards
     * @param cards		list of cards given
     */
    public static CardSet of(List<Card> cards) {
        
        CardSet cardSet = new CardSet(PackedCardSet.EMPTY);
        for (Card c: cards) {
        	cardSet = cardSet.add(c);
        }
         return cardSet;     
    }
    
    /**
     * Method used to construct a new CardSet instance from a valid "packed" long representing a CardSet
     * @throws IllegalArgumentException if the packed CardSet is incorrect
     * @param packed		            "packed" CardSet
     */
    public static CardSet ofPacked(long packed) {
       Preconditions.checkArgument(PackedCardSet.isValid(packed));
       
       return new CardSet(packed);   
    }
    
    /**
     * Returns the "packed" value representing the CardSet of this instance
     */
    public long packed() {
    	return this.cardSet;
    }
    
    /**
     * Tests if current CardSet instance contains no cards (is empty)
     * @return		true if empty, false otherwise
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(this.cardSet);
    }
    
    /**
     * Returns the number of cards in the CardSet
     */
    public int size() {
        return PackedCardSet.size(this.cardSet);
    }
    
    /**
     * Get the n-th card of the CardSet according to the index
     * @param index						n-th card to get
     * @throws IllegalArgumentException if the given index is less than 0 or greater than the index of the highest card in the cardSet
     * @return Card						instance of the wanted card
     */
    public Card get(int index) {
    	Preconditions.checkIndex(index, Long.SIZE);

    	int card = PackedCardSet.get(this.cardSet, index);
        return Card.ofPacked(card);
    }
    
    /**
     * Add a card to the CardSet
     * @param card		Card instance of the card to add to the CardSet
     * @return			new instance of the old CardSet containing also the added card
     */
    public CardSet add(Card card) {
    	
    	// validity of the card done in the "packed" method
        return new CardSet(PackedCardSet.add(this.cardSet, card.packed()));
    }
    
    /**
     * Remove a card from the CardSet
     * @param card		Card instance of the card to remove from the CardSet
     * @return			new instance of the old CardSet containing no more the removed card
     */
    public CardSet remove(Card card) {
    	
        return new CardSet(PackedCardSet.remove(this.cardSet, card.packed())); 
        
    }
    
    /**
     * Checks if a given card is present in the current CardSet
     * @param card		card to check if present in CardSet
     * @return			true if given card in set, false otherwise
     */
    public boolean contains(Card card) {
    	
        return PackedCardSet.contains(this.cardSet, card.packed());
        
    }
    
    /**
     * Returns a new CardSet containing all the cards not in the current CardSet (complement of the CardSet)
     */
    public CardSet complement() {
        return new CardSet(PackedCardSet.complement(this.cardSet));
        
    }
    
    /**
     * Returns a new CardSet containing all the cards of the current CardSet and the ones from the given CardSet
     * @param that		given CardSet
     */
    public CardSet union(CardSet that) {
    	
        return new CardSet(PackedCardSet.union(this.cardSet, that.packed()));
        
    }
    
    /**
     * Returns a new CardSet containing only present in both the current CardSet and the given one
     * @param that		given CardSet
     */
    public CardSet intersection(CardSet that) {
    	
        return new CardSet(PackedCardSet.intersection(this.cardSet, that.packed()));
    }
    
    /**
     * Returns a new CardSet containing all the cards of the current CardSet minus the ones from the given CardSet
     * @param that		given CardSet
     */
    public CardSet difference(CardSet that) {
    	
        return new CardSet(PackedCardSet.difference(this.cardSet, that.packed()));

    }
    
    /**
     * Returns only the cards of the given color from the current CardSet
     * @param color		given color to return
     */
    public CardSet subsetOfColor(Card.Color color) {
        return new CardSet(PackedCardSet.subsetOfColor(this.cardSet, color));

    }
    
    @Override
    public String toString() {
        return PackedCardSet.toString(this.cardSet);
    }
    
    @Override
    public boolean equals(Object other) {
        
        if(!(other instanceof CardSet)) {
            return false;
        }        
        return this.cardSet == ((CardSet)other).cardSet;  
    }
    
    @Override
    public int hashCode() {
       return Long.hashCode(this.cardSet);
    }
    
}
