package ch.epfl.javass.game;

import java.util.HashMap;

import java.util.Map;

import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Plays an example game with one MctsPlayer as player 2 and the rest as random and printing (still random) players 
 */
public final class MctsJassGame {
	  public static void main(String[] args) {
		 Map<PlayerId, Player> players = new HashMap<>();
	    Map<PlayerId, String> playerNames = new HashMap<>();

	    for (PlayerId pId: PlayerId.ALL) {
	      Player player = new RandomPlayer(0);
	      if (pId == PlayerId.PLAYER_1)
		  player = new PrintingPlayer(player);
	      players.put(pId, player);
	      playerNames.put(pId, pId.name());
	      if(pId == PlayerId.PLAYER_2) {
	    	  player = new MctsPlayer(pId, 2019, 1000);
		      players.put(pId, player);
		      playerNames.put(pId, pId.name());
	      }
	    }
	    JassGame g = new JassGame(0, players, playerNames);
	    while (! g.isGameOver()) {
	      g.advanceToEndOfNextTrick();
	      System.out.println("----");
	    }
	  }
	  
	}

	
	
	