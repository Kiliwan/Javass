package ch.epfl.javass.jass;

import java.util.Map; 

import ch.epfl.javass.jass.Card.Color;
/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Interface that offers all the base methods for the players 
 */
public interface Player {
	
	abstract Card cardToPlay(TurnState state, CardSet hand);
	abstract Color chooseTrump(CardSet hand);

	default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {}
	default void updateHand(CardSet newHand) {}
	default void setTrump(Color trump) {}
	default void updateTrick(Trick newTrick) {}
	default void updateScore(Score score) {}
	default void setWinningTeam(TeamId winningTeam) {}
	default void updateMelds(Map<PlayerId, String> playerMelds) {}
	default void setFirstPlayer(PlayerId player) {}

}
