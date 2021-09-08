package ch.epfl.javass.game;

import java.util.Random;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.TurnState;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Player that plays a random card from his hand 
 */
public final class RandomPlayer implements Player {
		  private final Random rng;

		  public RandomPlayer(long rngSeed) {
		    this.rng = new Random(rngSeed);
		  }

		  @Override
		  public Card cardToPlay(TurnState state, CardSet hand) {
		    CardSet playable = state.trick().playableCards(hand);
		    int nextCard = rng.nextInt(playable.size());
		    return playable.get(nextCard);
		  }

		@Override
		public Color chooseTrump(CardSet hand) {
			return Color.ALL.get(rng.nextInt(Color.COUNT));
		} 
	}
