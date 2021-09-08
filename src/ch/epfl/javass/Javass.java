package ch.epfl.javass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import ch.epfl.javass.net.RemotePlayerServer;
import ch.epfl.javass.net.StringSerializer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Javass extends Application{
	
	private  List<Text> texts = Arrays.asList(new Text(),new Text(),new Text(),new Text(),new Text(),new Text(),new Text(),new Text());
	private  List<RadioButton> allButtons = Arrays.asList(null, null, null, null);
	private Label menuMessage = new Label();
	private Text seed = new Text();
	
	
	private  Map<PlayerId, Player> players = new HashMap<>();
	private  Map<PlayerId, String> playerNames = new HashMap<>();
	private Random mainRandom;
	private long gameSeed;
	
	private final String[] defaultNames = {"Aline", "Bastien", "Colette", "David"};
	
	// time to wait
	public static final double SIM_TIME = 0.5; //in seconds
	public static final int TURN_TIME = 1000; // in milliseconds
	
	// default settings for simulated and remote players
	public static final int DEFAULT_ITERATIONS = 10000;
	public static final String DEFAULT_HOST = "localhost";
	
	/*
	 * 
	 * Main to lauch the application
	 * 
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("Javass");
		primaryStage.getIcons().add(new Image("file:src/ch/epfl/javass/icon_javass.png"));
		primaryStage.setResizable(false);
		
		GridPane layout = new GridPane();
		
		// general message
		layout.add(menuMessage, 0, 4, 4, 1);
		menuMessage.setVisible(true);
		menuMessage.setText("You do not have to fill all the text fields : "
							+ "default values are programmed "
							+ "but you should at least select the types of the 4 players");
		menuMessage.setMaxWidth(Double.MAX_VALUE);
		menuMessage.setAlignment(Pos.CENTER);
		
		layout.setStyle(""
				+ "-fx-hgap:10px;"
				+ "-fx-vgap:10px;"
				+ "-fx-padding:5px;"
				+ "-fx-background-color:whitesmoke;"
				+ "-fx-font:16 Optima;");
		
		Label title = new Label("Javass");
		title.setMaxWidth(Double.MAX_VALUE);
		title.setAlignment(Pos.CENTER);
		layout.add(title, 0, 0, 4, 1);

		// custom font for the title
		Font custom = Font.loadFont("file:src/ch/epfl/javass/COMICZ.TTF", 48);
		title.setFont(custom);
		
		// player interface settings
		for(PlayerId player : PlayerId.ALL) {
			layout.add(createPlayerPane(player), player.ordinal(), 1);
		}
		
		// launch options
		Labeled[] toStart = createButtonsAndLMessage();
		layout.addRow(2, createSeedPane(), getIP(), toStart[0], toStart[1]);
		

		Scene appScene = new Scene(layout, 1330, 400);
		
		// other general appearance settings
		setUserAgentStylesheet(STYLESHEET_MODENA);
		
		primaryStage.setScene(appScene);
		primaryStage.show();
	}
	
	/**
	 * Method to create the buttons for the local and remote launch of the game
	 * @return		array of buttons containing the local and remote button
	 */
	private Button[] createButtonsAndLMessage() {
		
		// local buttom
		Button launchLocal = new Button("Launch Local Game");
		launchLocal.setMaxWidth(Double.MAX_VALUE);
		launchLocal.setAlignment(Pos.CENTER);
		launchLocal.autosize();
		launchLocal.setOnAction(e -> {
			
			try {
				if(checkingAllArguments()) {
					menuMessage.setText("Game will start soon...");
					launchLocal(new Stage());
				}
			 } catch (Exception exception)  { 
				 menuMessage.setText("Can't launch a local game : either a IP Address of a remote player is wrong or you didn't select 4 players");
			 } 
		  
		});
		
		// remote button
		Button launchRemote = new Button("Launch Remote Game");
		launchRemote.setMaxWidth(Double.MAX_VALUE);
		launchRemote.setAlignment(Pos.CENTER);
		launchRemote.autosize();
		launchRemote.setOnAction(e ->  { 
			
			try {
				menuMessage.setText("Game will start as soon as the client is connected...");
				launchRemote(new Stage());
			 } catch (Exception exception)  { 
				menuMessage.setText("Can't launch a remote game : Check your internet connection");
			 } 
		}); 
		
		Button[] toReturn = {launchLocal, launchRemote};
		return toReturn;
	}
	
	/**
	 * Method to create the player panes
	 * @param pId		PlayerId of the player pane to create
	 * @return			Pane representing the player of the given id
	 */
	private GridPane createPlayerPane(PlayerId pId) {
		
		GridPane playerPane = new GridPane();
		
		Label id = new Label(pId.toString());
		id.setMaxWidth(Double.MAX_VALUE);
		id.setAlignment(Pos.CENTER);
		
		// player type setting
		GridPane buttons = new GridPane();
		ToggleGroup group = new ToggleGroup();
		
		RadioButton human = new RadioButton("Human Player");
		human.setToggleGroup(group);
		buttons.addRow(0, human);
		
		RadioButton simulated = new RadioButton("Simulated Player");
		simulated.setToggleGroup(group);
		buttons.addRow(1, simulated);
		
		RadioButton remote = new RadioButton("Remote Player");
		remote.setToggleGroup(group);
		buttons.addRow(2, remote);
		
		group.selectedToggleProperty().addListener((a,b,c) -> {
			allButtons.set(pId.ordinal(), (RadioButton) c);
		});
		
		// player name setting
		Label nameLabel = new Label("Name : ");
		TextField name = new TextField();
		
		// player special settings
		Label iteration = new Label("Iterations : ");
		iteration.visibleProperty().bind(simulated.selectedProperty());
		
		Label ip = new Label("IP Address : ");
		ip.visibleProperty().bind(remote.selectedProperty());
		
		TextField entry = new TextField();
		entry.visibleProperty().bind(remote.selectedProperty().or(simulated.selectedProperty()));
		
		playerPane.add(id, 0, 0, 2, 1);
		playerPane.add(buttons, 0, 1, 2, 1);
		playerPane.addRow(2, nameLabel, name);
		StackPane it_ip = new StackPane(iteration, ip);
		playerPane.addRow(3, it_ip, entry);
		
		playerPane.setStyle(""
				+ "-fx-border-radius: 25;"
				+ "-fx-background-radius: 25;"
				+ "-fx-padding: 10;"
				+ "-fx-background-color:lightgray;"
				+ "-fx-border-width: 2;"
				+ " -fx-border-style: solid;"
				+ " -fx-border-color: gray;");
		
		texts.get(pId.ordinal() * 2).textProperty().bind(name.textProperty());
		texts.get(pId.ordinal() * 2 + 1).textProperty().bind(entry.textProperty());
			
		return playerPane;
	}
	
	/**
	 * Method to create the seed Pane
	 * @return		Pane of the seed setting
	 */
	private GridPane createSeedPane() {
		
		GridPane seedPane = new GridPane();
		
		Label nbSeed = new Label("Seed : ");
		TextField seedField = new TextField();
		seedPane.addRow(0, nbSeed, seedField);
		seed.textProperty().bind(seedField.textProperty());
		
		seedPane.setStyle(""
				+ "-fx-border-radius: 25;"
				+ "-fx-background-radius: 25;"
				+ "-fx-padding: 10;"
				+ "-fx-background-color:lightgray;"
				+ "-fx-border-width: 2;"
				+ " -fx-border-style: solid;"
				+ " -fx-border-color: gray;");
		
		return seedPane;
	}
	
	/**
	 * Method to launch a remote game
	 * @param stage			stage in which to launch the game in
	 * @throws Exception	if execution cannot be completed
	 */
	private void launchRemote(Stage stage) throws Exception  { 
		menuMessage.setText("Game will start as soon as the client is connected..."); 
		
		// thread of the server connecting to the game and of the game 
		Thread serverThread = new Thread(() ->  {  
			RemotePlayerServer server = new RemotePlayerServer(new GraphicalPlayerAdapter()); 
			server.run(); 
		 }); 
		serverThread.setDaemon(true); 
		serverThread.start(); 
	 } 
	
	/**Method to launch a local game
	 * @param stage			stage in which to launch the game in
	 * @throws Exception	if exectution cannot be completed
	 */
	private void launchLocal(Stage stage) throws Exception  { 
		

		mainRandom = seed.getText().isEmpty() ? new Random() : new Random(gameSeed);		
		gameSeed = mainRandom.nextLong();
		
		for(int i = 0; i < PlayerId.COUNT; ++i) {
			PlayerId player = PlayerId.ALL.get(i);
			switch(allButtons.get(i).getText().charAt(0)) {
			
			case 'H' :  // for human playing on this computer
				createHumanPlayer(player);
				break;
		
			case 'S' : // for simulated player
				createSimulatedPlayer(player);
				break;
				
			
			case 'R' : // for remote player playing on another player
				createRemotePlayer(player);
				break;
		
			default : //defualt in case it was wrongly launched
				menuMessage.setText("Error : Incorrect argument(s)");
				break;
			}
		}
	    
		// thread of the actual game
		Thread gameThread = new Thread(() -> {
			JassGame jassGame = new JassGame(gameSeed, players, playerNames);
			while (!jassGame.isGameOver()) {
				try {
				jassGame.advanceToEndOfNextTrick();
				Thread.sleep(TURN_TIME);	// wait between each turn
				
				} catch (Exception e) {
					//menuMessage.setText("Couldn't continue the game");
				}
			}
		});
		gameThread.setDaemon(true);
		gameThread.start();
	}

	/**
	 * Method to create separately a human player
	 * @param pId	PlayerId of the player created
	 */
	private void createHumanPlayer(PlayerId pId) {
		players.put(pId, new GraphicalPlayerAdapter());
		playerNames.put(pId, texts.get(pId.ordinal() * 2).getText().isEmpty() ? defaultNames[pId.ordinal()] : texts.get(pId.ordinal() * 2).getText());
	}	

	/**
	 * Method to create separately a simulated player
	 * @param pId	PlayerId of the player created
	 */
	private void createSimulatedPlayer(PlayerId pId) {
		players.put(pId, new PacedPlayer(
			new MctsPlayer(pId, mainRandom.nextLong(), texts.get(pId.ordinal() * 2 + 1).getText().isEmpty() ? DEFAULT_ITERATIONS : Integer.parseInt(texts.get(pId.ordinal() * 2 + 1).getText())),
			SIM_TIME));
		playerNames.put(pId, texts.get(pId.ordinal() *2).getText().isEmpty() ?  defaultNames[pId.ordinal()] : texts.get(pId.ordinal() * 2).getText());
	}

	/**
	 * Method to create separately a distant player
	 * @param pId	PlayerId of the player created
	 */
	private void createRemotePlayer(PlayerId pId) {
		try {
			String ip = texts.get(pId.ordinal() * 2 + 1).getText();

			Player p = new RemotePlayerClient(ip.isEmpty() ? DEFAULT_HOST : ip);			
			players.put(pId, p);
		} catch (IOException e) {
			menuMessage.setText("Error -> can't create remote player : IP address must be missing");
		}
		playerNames.put(pId, texts.get(pId.ordinal() *2).getText().isEmpty() ? defaultNames[pId.ordinal()] : texts.get(pId.ordinal() * 2).getText());
	}
	
	/**
	 * Method to check all given arguments given in the graphical interface before starting a game and prints the corresponding warning otherwise
	 * @return		true if all arguments valid, false otherwise
	 */
	private boolean checkingAllArguments() {
		
		// test if the four player types have been selected
		for(int i=0;i<allButtons.size();i++) {
			if(allButtons.get(i)==null) {
				return false;
			}
		}
		
		// test if the seed is in the correct format
		if(!(seed.textProperty().get().isEmpty())){
			try {
				gameSeed = Long.parseLong(seed.getText());
				} catch (NumberFormatException e) {
					menuMessage.setText("Given seed is not a valid number");
					return false;
				}
		}
		
		// test if for each of the players the given arguments are correct
		for(int i = 0; i < allButtons.size(); ++i) {
			char playerType = allButtons.get(i).getText().charAt(0);
			String argument = texts.get(2 * i + 1).getText();
			
			if(!argument.isEmpty()) {
				switch(playerType) {
				
				case 'S' : 
					
					try {
						long l = Long.parseLong(texts.get(2 * i + 1).getText());
						if(l < 9) {
							menuMessage.setText("The iteration value must be at least 9");
							return false;
						}
					} catch (NumberFormatException e) {
						menuMessage.setText("One of the iteration number is not a valid number");
						return false;
					}
					break;
					
				case 'R' :
					
					String[] parts = StringSerializer.split("\\.", argument); 
					
					if (parts.length != 4) { 
						menuMessage.setText("One of the IP Address is not a valid address");
						return false; 
					} 
					
					for (String str : parts) { 
						int j = Integer.parseInt(str); 
						if ((j < 0) || (j > 255)) { 
							menuMessage.setText("One of the IP Address is not a valid address");
							return false; 
						} 
					}
					break;
				}
			}	
		}
		return true;
	}
	
	/**
	 * Method to get the local IP address of the computer on which the application is launched
	 * @return		Label containing the local IP address
	 */
	private Label getIP() { 
		String address = new String(); 
		try { 
			URL IpWebSite = new URL("http://bot.whatismyipaddress.com"); 
			BufferedReader sc = new BufferedReader(new InputStreamReader(IpWebSite.openStream())); 
			
			address = sc.readLine(); 
			
		} catch (Exception e) { 
	        address = "Can't get your IP Address \n Check your internet connection"; 
	    } 
		
		Label ip = new Label("Your IP Address is : " + address);
		ip.setMaxWidth(Double.MAX_VALUE);
		ip.setAlignment(Pos.CENTER);
		return ip;
	} 
} 