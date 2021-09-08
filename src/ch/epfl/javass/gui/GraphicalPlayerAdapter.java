package ch.epfl.javass.gui;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;
import javafx.application.Platform;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public class GraphicalPlayerAdapter implements Player {

	private final int QUEUE_CAPACITY = 1;
	
	private ScoreBean scoreB;
	private TrickBean trickB;
	private HandBean handB;
	private GraphicalPlayer graphicalPlayer;
	private ArrayBlockingQueue<Card> queueC;
	private ArrayBlockingQueue<Color> queueT;
	
	/**
	 * Constructor for the graphical player adapter who will make the communication between the game thread and the JavaFX thread
	 */
	public GraphicalPlayerAdapter() {
		this.scoreB = new ScoreBean();
		this.trickB = new TrickBean();
		this.handB = new HandBean();
		this.queueC = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
		this.queueT = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
	}
	
	@Override
	public void updateMelds(Map<PlayerId, String> playerMelds) {

		Platform.runLater(() -> {
			handB.setMelds(playerMelds); 
		});
	}
	
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		// create the actual graphical player for the human player
		GraphicalPlayer graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreB, trickB, handB, queueT, queueC);
		
		this.graphicalPlayer = graphicalPlayer;
		
		Platform.runLater(() -> { this.graphicalPlayer.createStage().show(); });
	}
	
	@Override
	public void setWinningTeam(TeamId winningTeam) {
		Platform.runLater(() -> { scoreB.setWinningTeam(winningTeam); });
	}
	
	@Override
	public void setTrump(Color trump) {
		Platform.runLater(() -> { trickB.setTrumpProperty(trump); });
	}
	
	@Override
	public void updateHand(CardSet newHand) {
		Platform.runLater(() -> { handB.setHand(newHand); });
	}
	
	@Override
	public void updateScore(Score score) {
		for(TeamId t : TeamId.ALL) {
			Platform.runLater(() -> { scoreB.setGamePoints(t, score.gamePoints(t)); });
			Platform.runLater(() -> { scoreB.setTotalPoints(t, score.totalPoints(t)); });
			Platform.runLater(() -> { scoreB.setTurnPoints(t, score.turnPoints(t)); });
		}
	}
	
	@Override
	public void updateTrick(Trick newTrick) {
		Platform.runLater(() -> { trickB.setTrickProperty(newTrick); });
	}
	
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		
		Platform.runLater(() -> {
			handB.setPlayableCards(state.trick().playableCards(hand));
		});
		
		// take card from communication queue to play it
		try {
			Card c = queueC.take();
			handB.setPlayableCards(CardSet.EMPTY);
			return c;
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
			throw new Error();
		}
	}
	
	@Override
	public Color chooseTrump(CardSet hand) {
		
		//take the trump in the communication queue
		try {
			Color c = queueT.take();
			return c;
		} catch (InterruptedException e) {
			System.out.println("Interrupted");
			throw new Error();
		}
	}
	
	@Override
	public void setFirstPlayer(PlayerId player) {
		Platform.runLater(() -> { trickB.setFirstProperty(player); });
	}

}
