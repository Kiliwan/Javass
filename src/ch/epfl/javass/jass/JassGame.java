package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class JassGame {
	
	private final Random shuffleRng;
	
	private final Map<PlayerId, Player> players;
	private final Map<PlayerId, String> playerNames;
	private final Map<PlayerId, MeldSet> playerMelds;
	private final Map<PlayerId, String> playerMeldsString;
	
	private TurnState turnState;
	
	private Map<PlayerId, CardSet> playerHands;
	private PlayerId firstPlayer;
	
	/**
	 * Constructor of game instance
	 * @param rngSeed		seed for random values in the game
	 * @param players		players participating in the game
	 * @param playerNames	names of the players participating in the game
	 */
	public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
		
		Random rng = new Random(rngSeed);
		this.shuffleRng = new Random(rng.nextLong());
		
		this.players = Collections.unmodifiableMap(new EnumMap<>(players));
		this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
		this.playerHands = new HashMap<>();
		this.playerMelds = new HashMap<>();
		this.firstPlayer = firstPlayer();
		this.playerMeldsString = new HashMap<>();
		for(PlayerId p : PlayerId.ALL) {
			players.get(p).setPlayers(p, Collections.unmodifiableMap(this.playerNames));
			players.get(p).setFirstPlayer(firstPlayer);
		}
		
		shuffleAndDistribute();

		Color trump = players.get(firstPlayer).chooseTrump(playerHands.get(firstPlayer));
		this.turnState = TurnState.initial(trump, Score.INITIAL, firstPlayer);
		
		for(PlayerId p : PlayerId.ALL) {
			turnState = turnState.withNewMeld(playerMelds.get(p), p.team());
			players.get(p).updateMelds(playerMeldsString);
			players.get(p).updateScore(turnState.score());
			players.get(p).setTrump(trump);
		}
	}
	
	/**
	 * Checks if game is over, so as soon as one of the two teams reach 1000 total points
	 * @return		true if the game is over, false otherwise
	 */
	public boolean isGameOver() {
		return turnState.score().totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS || 
			   turnState.score().totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS;
	}
	
	/**
	 * Sets the winning team and updates the score for all the players of the game
	 * @param team   winning team of the game   
	 */
	private void win(TeamId team) {
		for(PlayerId p : PlayerId.ALL) {
			players.get(p).setWinningTeam(team);
			players.get(p).updateScore(turnState.score());
		}
	}
	
	/**
	 * As long as the game is not over, advances to next trick.
	 * If it is the end of a turn, prepares to move to the next one, otherwise continues to play the turn.
	 * In each trick, asks to the next player which card he wants to play and collects the trick if full
	 */
	public void advanceToEndOfNextTrick() {
		if(!isGameOver()) {

			if(turnState.isTerminal()) { // every new turn
				
				turnState = turnState.withTrickCollected();
				shuffleAndDistribute();
				
				firstPlayer = PlayerId.ALL.get((firstPlayer.ordinal() + 1) % PlayerId.COUNT); // +1 to get nextPlayer

				for(PlayerId p : PlayerId.ALL) {
					players.get(p).setFirstPlayer(firstPlayer);
				}
				Color trump = players.get(firstPlayer).chooseTrump(playerHands.get(firstPlayer));

				turnState = TurnState.initial(trump, turnState.score().nextTurn(), firstPlayer);
				
				
				for(PlayerId p : PlayerId.ALL) {
					//turnState = turnState.withNewMeld(playerMelds.get(p), p.team());
					players.get(p).updateMelds(playerMeldsString);
					players.get(p).updateScore(turnState.score());
					players.get(p).setTrump(trump);
				}
			}
			if(turnState.trick().isFull()) {
				turnState = turnState.withTrickCollected();
			}
			
			if(turnState.trick().isEmpty()) {
				
				for(PlayerId p : PlayerId.ALL) {
					players.get(p).updateScore(turnState.score());
					players.get(p).updateTrick(turnState.trick());
				}
			}

			while(!turnState.trick().isFull()) {
				
				PlayerId currentPlayer = turnState.nextPlayer();
				CardSet playerHand = playerHands.get(currentPlayer);
				Card cardToPlay = players.get(currentPlayer).cardToPlay(turnState, playerHand);

				turnState = turnState.withNewCardPlayed(cardToPlay);

				playerHands.replace(currentPlayer, playerHand, playerHand.remove(cardToPlay));
				players.get(currentPlayer).updateHand(playerHands.get(currentPlayer));
				
				for(PlayerId p : PlayerId.ALL) {
					players.get(p).updateTrick(turnState.trick());
				}	
				
			}
			
			if(isGameOver()) {
				TeamId t = turnState.score().totalPoints(TeamId.TEAM_1) > turnState.score().totalPoints(TeamId.TEAM_2) ? TeamId.TEAM_1 : TeamId.TEAM_2;
				win(t);
			}
		}
	} 
	
	/**
	 * Defines the first player to play in the game, which is the one that have the seven of clubs in his hand, if no one is found the first player of the enumeration will be returned
	 * @return		PlayerId of the first player to play in the game
	 */
	private PlayerId firstPlayer() {
		
		Card sevenHeart = Card.of(Color.DIAMOND, Rank.SEVEN);
		
		for(PlayerId player : playerHands.keySet()) {
			if(playerHands.get(player).contains(sevenHeart)) {
				return player;
			}
		}
		return PlayerId.PLAYER_1;
	}
	
	/**
	 * Shuffles randomly the cards and distributes them to the players and find the melds
	 */
	private void shuffleAndDistribute() {

		List<Card> deck = new ArrayList<>();
		
		List<PlayerId> playersIds = new ArrayList<>(players.keySet());
		
		for(int i = 0; i < CardSet.ALL_CARDS.size(); ++i) {
			deck.add(CardSet.ALL_CARDS.get(i));
		}
		
		Collections.shuffle(deck, shuffleRng); // shuffling of all cards according to the "random" long seed shuffleRng
		
		for(int i = 0; i < players.size(); ++i) {
			CardSet playerCards = CardSet.EMPTY;
			for(int j = 0; j < Jass.HAND_SIZE; ++j) {
				playerCards = playerCards.add(deck.get(i*Jass.HAND_SIZE +j));
			}
			 
		PlayerId thisOne = playersIds.get(i);
			players.get(thisOne).updateHand(playerCards);
			playerHands.put(thisOne, playerCards); // put cards in each player's hand
		}
		
		for(PlayerId player : playerHands.keySet()) {
			CardSet hand = playerHands.get(player);
			Set<Card> cards = new HashSet<>();
			for(int i=0;i<hand.size();i++) {
				cards.add(hand.get(i));
			}
			
			MeldSet melds = MeldSet.getBestMeldSet(cards);
			playerMelds.put(player, melds);
			playerMeldsString.put(player, melds.toString());
		}
	}
}
