package ch.epfl.javass.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public class GraphicalPlayer {
	
	private final int TRUMP_PREFERRED_WIDTH = 120;
	private final int TRUMP_PREFERRED_HEIGHT = 120;
	
	private final int CARD_SMALL = 160;
	private final int CARD_SMALL_WIDTH = 80;
	private final int CARD_SMALL_HEIGHT = 120;
	private final int CARD_BIG = 240;
	private final int CARD_BIG_WIDTH = 120;
	private final int CARD_BIG_HEIGHT = 180;
	
	private final double FORBIDDEN_CARD_OPACITY = 0.2;
	
	private PlayerId underlyingPlayer;
	private Map<PlayerId, String> playerNames;
	private TrickBean trickB;
	private ScoreBean scoreB;
	private HandBean handB;
	private ArrayBlockingQueue<Card> queueC;
	private ArrayBlockingQueue<Color> queueT;
	
	/**
	 * Constructor for the graphical player (interface for the human player)
	 * @param underlyingPlayer	underlying player represented
	 * @param playerNames		map of the PlayerIds of the players related to their names
	 * @param scoreB			ScoreBean of the game
	 * @param trickB			TrickBean of the game
	 * @param handB				HandBean of the game
	 * @param queue				waiting queue for communication between the threads
	 */
	public GraphicalPlayer(PlayerId underlyingPlayer, Map<PlayerId, String> playerNames, ScoreBean scoreB, TrickBean trickB, HandBean handB, ArrayBlockingQueue<Color> queueT, ArrayBlockingQueue<Card> queueC) {
		this.underlyingPlayer = underlyingPlayer;
		this.playerNames = playerNames;
		this.trickB = trickB;
		this.scoreB = scoreB;
		this.handB = handB;
		this.queueC = queueC;
		this.queueT = queueT;
	}
	
	/**
	 * Create the full stage of the game for the graphical player instance
	 * @return	full stage of the game
	 */
	public Stage createStage() { 
		Pane score = createScorePane();
		
		Pane trick = createTrickPane();

		Pane hand = createHandPane();
		
		Pane trump = createTrumpPane();
		
		Pane melds = createMeldPane();

		Pane[] victoryPanes = new Pane[TeamId.COUNT];
		for(TeamId t : TeamId.ALL)
			victoryPanes[t.ordinal()] = createVictoryPaneTeam(t);
		
		BorderPane main = new BorderPane(trick, score, trump, hand, melds);
		
		main.setStyle("-fx-font: 16 Optima; -fx-padding: 5px; -fx-background-color: lightgray;"); 
		
		StackPane game = new StackPane(victoryPanes[0], main, victoryPanes[1]);
		
		Stage stage = new Stage();
		stage.setWidth(1024);
		stage.setHeight(800);
		stage.setScene(new Scene(game));
		return stage;
	}
	
	/**
	 * Method to create the meld pane of the game separately
	 * @return
	 */
	private Pane createMeldPane() {
		
		VBox melds = new VBox();
		
		for(int i = 0; i < PlayerId.COUNT; i++) {

			PlayerId player = PlayerId.ALL.get(i);
			Label playerLabel = new Label(playerNames.get(player));
			Label meldlabel = new Label();
			meldlabel.textProperty().bind(Bindings.valueAt(handB.meldsProperty(), player));
			
			melds.getChildren().add(playerLabel);
			melds.getChildren().add(meldlabel);
		}
		
		melds.setAlignment(Pos.CENTER);
		melds.setPrefWidth(Double.MAX_VALUE);
		melds.setSpacing(20);
		
		Label title = new Label("MELDS");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		
		BorderPane meldPane = new BorderPane();
		meldPane.setTop(title);
		meldPane.setCenter(melds);
		
		meldPane.setStyle("-fx-border-width: 2px 0px; -fx-border-style: solid;");
		meldPane.setMaxWidth(200);
		return meldPane;
	}
	
	private Pane createTrumpPane() {
		
		VBox trumps = new VBox();
		trumps.setMaxWidth(Double.MAX_VALUE);
		trumps.setAlignment(Pos.CENTER);
		trumps.setSpacing(20);
		
		trumps.setDisable(true);
		trumps.setOpacity(0.5);
		
		Label title = new Label("TRUMP CHOICE");
		title.setAlignment(Pos.CENTER);
		title.setMaxWidth(Double.MAX_VALUE);
		
		Label choose = new Label();
		choose.setText("Not your turn to choose the trump");
		choose.setAlignment(Pos.CENTER);
		choose.setMaxWidth(Double.MAX_VALUE);
		choose.setWrapText(true);
		choose.setPadding(new Insets(15));;
		
		trickB.firstProperty().addListener((a,b,c) -> {
			if(c.equals(underlyingPlayer)) {
				trumps.setDisable(false);;
				trumps.setOpacity(1);
				choose.setText(" It's your turn to start\n  Choose the trump");
			}
		});
		
		for(Color c : Card.Color.ALL) {
			
			ObjectProperty<Color> trumpObject = new SimpleObjectProperty<>();
			trumpObject.set(c);

			ImageView trump = new ImageView();
			trump.setImage(trumpImage(c));
			trump.setFitWidth(80);
			trump.setFitHeight(80);
			
			trump.setOnMouseClicked(e -> {
				trickB.setTrumpProperty(trumpObject.get());
				trumps.setOpacity(FORBIDDEN_CARD_OPACITY);
				trumps.setDisable(true);
				choose.setText("Not your turn to choose the trump");
				try {
					queueT.put(trumpObject.get());
				} catch (InterruptedException e1) {
					System.out.println("Interrupted");
				}				
			});
			
			trumps.getChildren().add(trump);
		};
		
		BorderPane trumpPane = new BorderPane();
		
		trumpPane.setTop(title);
		trumpPane.setCenter(trumps);
		trumpPane.setBottom(choose);
		trumpPane.setMaxWidth(200);
		
		trumpPane.setStyle(" -fx-border-width: 2px 0px; -fx-border-style: solid;" );

		return trumpPane;
	}
	
	/**
	 * Method to create the score pane of the game separately
	 * @return	score pane of the game
	 */
	private Pane createScorePane() {

		GridPane scorePane = new GridPane();
	
		for(int i = 0; i < TeamId.COUNT; ++i) {
			List<PlayerId> players = new ArrayList<>();
			for(PlayerId p : PlayerId.ALL) {
				if(p.team() == TeamId.ALL.get(i)) {
					players.add(p);
				}
			}
			
			String team = this.playerNames.get(players.get(0)) + " et " + playerNames.get(players.get(1)) + ": ";
			
			Label turnPoints = new Label();
			turnPoints.textProperty().bind(scoreB.turnPointsProperty(TeamId.ALL.get(i)).asString());
			
			Label gamePoints = new Label();
			gamePoints.textProperty().bind(scoreB.gamePointsProperty(TeamId.ALL.get(i)).asString());
			
			// point difference since the last trick
			Label trickDiff = new Label();
			scoreB.turnPointsProperty(TeamId.ALL.get(i)).addListener( (a,b,c) -> 
				trickDiff.setText(" (" + "+" + (c.intValue() == 0 ? 0 : Integer.toString(c.intValue() - b.intValue())) + ")" )
			);
			
			scorePane.add(new Label(team), 0, i);
			scorePane.add(turnPoints, 1, i);
			scorePane.add(trickDiff, 2, i);
			scorePane.add(new Label(" / Total : "), 3, i);
			scorePane.add(gamePoints, 4, i);

		}
		
		scorePane.setAlignment(Pos.CENTER);
		scorePane.maxHeight(Double.MAX_VALUE);
		
		return scorePane;
	}
	
	/**
	 * Method to create the trick pane of the game separately
	 * @return	trick pane of the game
	 */
	private Pane createTrickPane() {
		GridPane trickPane = new GridPane();

		ImageView trump = new ImageView();
		trump.setFitWidth(TRUMP_PREFERRED_WIDTH);
		trump.setFitHeight(TRUMP_PREFERRED_HEIGHT);
		trickB.trumpProperty().addListener((a,b,c) -> trump.imageProperty().set(trumpImage(trickB.trumpProperty().get())));
		
		trickPane.add(trump, 1, 1);
		
		ObservableMap<Card, Image> cards = cardNameSet(CARD_BIG); 
				
		for(int i = 0; i < PlayerId.COUNT; ++i) {
			// always start with the graphical player so it is always at the bottom position for us
			PlayerId player = PlayerId.ALL.get((underlyingPlayer.ordinal() + i)%4);
						
			Rectangle halo = new Rectangle();
			halo.setWidth(CARD_BIG_WIDTH);
			halo.setHeight(CARD_BIG_HEIGHT);
			halo.setStyle("-fx-arc-width: 20; -fx-arc-height: 20; -fx-fill: transparent; -fx-stroke: lightpink; -fx-stroke-width: 5; -fx-opacity: 0.5;");
			halo.setEffect(new GaussianBlur(4));
			halo.visibleProperty().bind(trickB.winningPlayerProperty().isEqualTo(player));

			
			 
			ImageView card = new ImageView();
			card.setFitWidth(CARD_BIG_WIDTH);
			card.setFitHeight(CARD_BIG_HEIGHT);
			card.imageProperty().bind(Bindings.valueAt(cards, Bindings.valueAt(trickB.trickProperty(), player)));
			
			
			VBox box = new VBox(1.0, new Label(playerNames.get(player)), new StackPane(card, halo));			
			box.setAlignment(Pos.CENTER);
			
			switch(i) {
				// special case because the name is at the bottom
				case 0 : box = new VBox(1.0, new StackPane(card, halo), new Label(playerNames.get(player)));
						 box.setAlignment(Pos.CENTER);
					   	 trickPane.add(box, 1, 2);
					break;					
				case 1 : trickPane.add(box, 2, 0, 1, 3);
					break;
					
				case 2 : trickPane.add(box, 1, 0);
					break;
					
				case 3 : trickPane.add(box, 0, 0, 1, 3);
					break;	
			}
		}
		trickPane.setAlignment(Pos.CENTER);
		trickPane.setStyle("-fx-background-color: whitesmoke; -fx-border-width: 2px 0px; -fx-border-style: solid;");
		
		return trickPane;
	}
	
	/**
	 * Method to create the hand pane of the game separately
	 * @return	hand pane of the game
	 * @throws	InterruptedException thrown if thread was occupied and interrupted
	 */
	private Pane createHandPane() {
		
		HBox handPane = new HBox(1);
		
		ObservableMap<Card, Image> cards = FXCollections.observableMap(cardNameSet(CARD_SMALL)); 
		
		for(int i=0; i < handB.handProperty().size();i++) {
			
			ObjectProperty<Card> cardObject = new SimpleObjectProperty<>();
			cardObject.bind(Bindings.valueAt(handB.handProperty(),i));
			
			ImageView card = new ImageView();
						
			Image cardImage = cards.get(cardObject.get());
			card.setFitWidth(CARD_SMALL_WIDTH);
			card.setFitHeight(CARD_SMALL_HEIGHT);
			card.setImage(cardImage);
			handPane.getChildren().add(card);
			
			card.imageProperty().bind(Bindings.valueAt(cards, Bindings.valueAt(handB.handProperty(), i)));
			
			card.setOnMouseClicked(e -> {
				try {
					queueC.put(cardObject.get());
				} catch (InterruptedException e1) {
					System.out.println("Interrupted");
				}
			});
			
			BooleanBinding isPlayable = Bindings.createBooleanBinding(
					
					() -> { 
						return handB.playableCardsProperty().contains(cardObject.get());
					}, handB.handProperty(), handB.playableCardsProperty());
			
			BooleanBinding wasPlayed = Bindings.createBooleanBinding( 
					() -> { 
						return handB.handProperty().contains(cardObject.get());
					}, handB.handProperty());
			
			DoubleBinding visibleCondition = Bindings.when(isPlayable).then(1).otherwise(FORBIDDEN_CARD_OPACITY);

		
			card.disableProperty().bind(isPlayable.not());
			card.opacityProperty().bind(visibleCondition);
			card.visibleProperty().bind(wasPlayed);
		}
		handPane.setAlignment(Pos.CENTER);
		handPane.setSpacing(5);
		
		return handPane;
	}
	
	/**
	 * Method to create the victory pane of the game separately
	 * @return	victory pane of the game
	 */
	private Pane createVictoryPaneTeam(TeamId team) {
		
		BorderPane victoryPane = new BorderPane();
		Text text = new Text();
		StringBuilder sb = new StringBuilder();
		// stacked on top of the main game field but visible only when the corresponding team wins
		BooleanBinding win = scoreB.totalPointsProperty(team).greaterThanOrEqualTo(Jass.WINNING_POINTS);
		victoryPane.visibleProperty().bind(win);
		victoryPane.setStyle("-fx-font: 16 Optima; -fx-background-color: white;");
			
			
		
		for(PlayerId player : playerNames.keySet()) {
			if(player.team().ordinal() == team.ordinal()) {
				if(sb.length()!=0) { // no "et" for first player
					sb.append(" et ");
				}
			sb.append(playerNames.get(player));
			}
		}

		sb.append(" ont gagné avec ");
		
		text.textProperty().bind(Bindings.format(sb.toString() + "%d contre %d ", scoreB.totalPointsProperty(team), scoreB.totalPointsProperty(team.other())));
		victoryPane.setCenter(text);
		
		return victoryPane;
	}
	
	/**
	 * Returns a map linking the cards to the respective path names corresponding to the width given
	 * @param width		given width
	 * @return			map of the cards with their corresponding path names
	 */
	private ObservableMap<Card, Image> cardNameSet(int width){
		
		ObservableMap<Card, Image> nameSet = FXCollections.observableHashMap();
		for(int i = 0; i < CardSet.ALL_CARDS.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append("card_");
			sb.append(CardSet.ALL_CARDS.get(i).color().ordinal());
			sb.append('_');
			sb.append(CardSet.ALL_CARDS.get(i).rank().ordinal());
			sb.append('_');
			sb.append(width);
			sb.append(".png");
			nameSet.put(CardSet.ALL_CARDS.get(i), new Image(sb.toString()));
		}
		return FXCollections.unmodifiableObservableMap(nameSet);
	}
	
	/**
	 * Returns the name path for a trump image given the color
	 * @param c		trump color given
	 * @return		path name of the trump given color
	 */
	private Image trumpImage(Color c) {
		return new Image("trump_" + Integer.toString(c.ordinal()) + ".png");
	}
}
