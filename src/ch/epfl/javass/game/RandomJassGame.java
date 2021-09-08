package ch.epfl.javass.game;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Plays an example game with only random players 
 */
public final class RandomJassGame {
	  public static void main(String[] args) {
	    Map<PlayerId, Player> players = new HashMap<>();
	    Map<PlayerId, String> playerNames = new HashMap<>();

	    for (PlayerId pId: PlayerId.ALL) {
	      Player player = new RandomPlayer(2019);
	      if (pId == PlayerId.PLAYER_1)
		  player = new PrintingPlayer(player);
	      players.put(pId, player);
	      playerNames.put(pId, pId.name());
	    }
	    JassGame g = new JassGame(2019, players, playerNames);
	    while (! g.isGameOver()) {
	      g.advanceToEndOfNextTrick();
	      System.out.println("----");
	    }
	  }
	}
	
	
	

