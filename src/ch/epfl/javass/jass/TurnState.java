package ch.epfl.javass.jass;

import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */
public final class TurnState {
    
    private final long pkScore;
    private final long pkUnplayedCards;
    private final int pkTrick;
    
    /**
     * Constructor of TurnState instance
     */
    private TurnState(Score score, Trick trick, CardSet cardSet) {
        this.pkScore = score.packed(); 
        this.pkTrick = trick.packed();
        this.pkUnplayedCards = cardSet.packed();
    }
    
    /**
     * Method used to create new TurnState instance at the beginning of the game (first turn)
     * @param trump			trump at the beginning of the match
     * @param score			score at the beginning of the match
     * @param firstPlayer	first player to play at the beginning of the match
     */
    public static TurnState initial(Color trump, Score score, PlayerId firstPlayer) {
        return new TurnState(score, Trick.firstEmpty(trump, firstPlayer), CardSet.ALL_CARDS);
    }
    
    /**
     * Method used to create new TurnState instance from the "packed" components of it
     * @param pkScore			"packed" score
     * @param pkUnplayedCards	"packed" cardset of the unplayed cards
     * @param pkTrick			"packed" trick
     */
    public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
        
    	// arguments already tested in their constructor
        return new TurnState(Score.ofPacked(pkScore),
        		             Trick.ofPacked(pkTrick), 
        		             CardSet.ofPacked(pkUnplayedCards));
    }
   
    /**
     * Getter for "packed" score of the turn state
     */
    public long packedScore() {
        return pkScore; 
    }
    
    /**
     * Getter for "packed" cardset of the unplayed cards of the turn state
     */
    public long packedUnplayedCards() {
        return pkUnplayedCards; 
    }

    /**
     * Getter for "packed" trick of the turn state
     */
    public int packedTrick() {
        return pkTrick; 
    }
    
    /**
     * Getter for Score instance of the turn state
     */
    public Score score() {
        return Score.ofPacked(pkScore); 
    }
    
    /**
     * Getter for CardSet instance of the unplayed cards of the turn state
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(pkUnplayedCards); 
    }

    /**
     * Getter for Trick instance of the turn state
     */
    public Trick trick() {
        return Trick.ofPacked(pkTrick);
    }
    
    /**
     * Tests if the turn state is terminal, which means that the last trick of the turn was just finished
     */
    public boolean isTerminal() {
    	
    	// if last trick of the turn was played and collected, the next one will be set to an invalid one and detected here
    	return packedTrick() == PackedTrick.INVALID || (trick().isFull() && trick().isLast());
   	}
    
    
    /**
     * Returns the next player to play in the turn
     */
    public PlayerId nextPlayer() {
    	if(trick().isFull()) {
            throw new IllegalStateException();
        }
        return trick().player(trick().size());
    }

    /**
     * Returns a new TurnState instance after the given card was played
     * @param card		given card to play
     */
    public TurnState withNewCardPlayed(Card card) {
    	if(trick().isFull() || !PackedCardSet.contains(pkUnplayedCards, card.packed())) {
            throw new IllegalStateException();
        }
    	return new TurnState(score(), 
	                         trick().withAddedCard(card), 
	                         unplayedCards().remove(card));
    }
    
    /**
     * Returns a new TurnState instance after the trick was collected
     */
    public TurnState withTrickCollected() {
        if(!trick().isFull()) {
            throw new IllegalStateException();
        }
        return new TurnState(score().withAdditionalTrick(trick().winningPlayer().team(), 
	                         PackedTrick.points(packedTrick())), 
	                         trick().nextEmpty(), unplayedCards());
    }
    
    /**
     * Returns a new TurnState instance after the given card was played and the trick collected if full
     * @param card		given card
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
    	if(trick().isFull()) {
            throw new IllegalStateException();
        }
    	
    	TurnState evolved = new TurnState(score(), trick(), unplayedCards());
    	evolved = evolved.withNewCardPlayed(card);
    	
    	return (evolved.trick().isFull() ? evolved.withTrickCollected() : evolved);
    }
    
    /**
     * Returns a new TurnState instance with the points of the melds added to the given team
     * @param melds		set of melds
     * @param team		team to which the melds belong to
     */
     public TurnState withNewMeld(MeldSet melds, TeamId team) {
    	return new TurnState(score().meldPoints(melds, team), trick(), unplayedCards());
    }
    
}