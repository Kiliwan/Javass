package ch.epfl.javass.game;

import java.util.Map;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Player that prints all his actions 
 */
public final class PrintingPlayer implements Player {
		  private final Player underlyingPlayer;

		  public PrintingPlayer(Player underlyingPlayer) {
		    this.underlyingPlayer = underlyingPlayer;
		  }

		  @Override
		  public Card cardToPlay(TurnState state, CardSet hand) {
			Card c = underlyingPlayer.cardToPlay(state, hand);
		    System.out.println("My turn : I played " + c.toString());
		    return c;
		  }

		@Override
		public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
			underlyingPlayer.setPlayers(ownId, playerNames);
			System.out.println("I am : " + ownId.toString());
			System.out.println("and all players are :" + playerNames.keySet());
			System.out.println();

		}

		@Override
		public void updateHand(CardSet newHand) {
			underlyingPlayer.updateHand(newHand);
			System.out.println("Hand is "+ newHand.toString());
		}

		@Override
		public void setTrump(Color trump) {
			underlyingPlayer.setTrump(trump);
			System.out.println("Trump is "+trump.toString()+"\n");
		}

		@Override
		public void updateTrick(Trick newTrick) {
			underlyingPlayer.updateTrick(newTrick);
			System.out.println("Trick is : "+ newTrick.toString());
		}

		@Override
		public void updateScore(Score score) {
			underlyingPlayer.updateScore(score);
			System.out.println("Score is " + score.toString());
		}

		@Override
		public void setWinningTeam(TeamId winningTeam) {
			underlyingPlayer.setWinningTeam(winningTeam);
			System.out.println("Winning team is " + winningTeam.toString());
		}

		@Override
		public Color chooseTrump(CardSet hand) {
			return underlyingPlayer.chooseTrump(hand);
		}
	}