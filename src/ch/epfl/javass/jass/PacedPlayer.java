package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 * Made to be sure that a simulated player takes always the same minimum time to play, no matter his cards
 */
public final class PacedPlayer implements Player {
	
	private final Player underlyingPlayer;
	private final double minTime;
	
	/**
	 * Constructor for paced (simulated) player
	 * @param underlyingPlayer		underlying player represented
	 * @param minTime				minimum time the player takes before playing in seconds
	 */
	public PacedPlayer(Player underlyingPlayer, double minTime) {
		if(minTime<0) {
			throw new IllegalArgumentException();
		}
		this.underlyingPlayer = underlyingPlayer;
		this.minTime = minTime;
	}

	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		
		long time = System.currentTimeMillis(); // catch the current system time
		Card card = underlyingPlayer.cardToPlay(state, hand);
		double remaining = (minTime * 1000) - (System.currentTimeMillis() - time);  // times 1000 because minTime is in second 
		
		if(remaining > 0) { // if there is time left before the minimum threshold, wait until then to finish playing
			try {
				  Thread.sleep((long)remaining);
			} catch (InterruptedException e) { /* ignore */ }
		}
		return card;
	}

	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		underlyingPlayer.setPlayers(ownId, playerNames);
	}

	@Override
	public void updateHand(CardSet newHand) {
		underlyingPlayer.updateHand(newHand);
	}

	@Override
	public void setTrump(Color trump) {
		underlyingPlayer.setTrump(trump);
	}

	@Override
	public void updateTrick(Trick newTrick) {
		underlyingPlayer.updateTrick(newTrick);
	}

	@Override
	public void updateScore(Score score) {
		underlyingPlayer.updateScore(score);
	}

	@Override
	public void setWinningTeam(TeamId winningTeam) {
		underlyingPlayer.setWinningTeam(winningTeam);
	}

	@Override
	public Color chooseTrump(CardSet hand) {
		return underlyingPlayer.chooseTrump(hand);
	}
}
