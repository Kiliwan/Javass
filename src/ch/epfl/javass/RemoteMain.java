package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public class RemoteMain extends Application{

	/**
     * Main to join a distant game
     */
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// thread of the server connecting to the game and of the game
		Thread serverThread = new Thread(() -> {
			RemotePlayerServer server = new RemotePlayerServer(new GraphicalPlayerAdapter());
			System.out.println("La partie commencera à la connextion du client...");
			server.run();
		});
		serverThread.setDaemon(true);
		serverThread.start();
	}

}
