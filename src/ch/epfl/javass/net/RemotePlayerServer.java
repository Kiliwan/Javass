package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

public final class RemotePlayerServer{
	
	public static final int PORT = 5108;
	
	private Player localPlayer;
	
	/**
	 * Constructs a new instance RemotePlayerServer 
	 * @param localPlayer		 player whose the server will receive instructions
	 */
	public RemotePlayerServer(Player localPlayer) {
		this.localPlayer = localPlayer;	
	}
	
	/**
	 * Receives the instruction from the client and do the appropriate actions according to the switch case
	 * Updates the different parameters of the game (hand, score, trick, etc...)
	 * Do not return anything except for CARD that sends back to the client the card to play
	 * Run indefinitely and waits for instructions until the game is ended (until the winning team is defined) 
	 * @throws	IOExcetpion
	 */
	public void run() {

		try (ServerSocket s0 = new ServerSocket(PORT);

				Socket s = s0.accept();
				BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(),US_ASCII));
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),US_ASCII)))
				{
				
					// runs until the game is won and the score is defined 
					String command = r.readLine();
					while(!command.equals(null)) {
						String[] splittedCommand = StringSerializer.split(" ", command);

						// switch case that dispatches the actions to dot according to the instruction received
						switch(JassCommand.valueOf(splittedCommand[0])) {
						
						case MELD:
							String[] melds = StringSerializer.split(",", splittedCommand[1]);
							Map<PlayerId, String> playerMelds = new HashMap<>();
							
							for(int i=0;i<melds.length;i++) {
								playerMelds.put(PlayerId.ALL.get(i), StringSerializer.deseralizeString(melds[i]));
							}
							localPlayer.updateMelds(playerMelds);
							break;
						
						case PLRS:
							String[] names = StringSerializer.split(",", splittedCommand[2]);
							Map<PlayerId, String> playerNames = new HashMap<>();
							
							for(int i=0;i<names.length;i++) {
								names[i] = StringSerializer.deseralizeString(names[i]);
								playerNames.put(PlayerId.ALL.get(i), names[i]);
							}
							
							PlayerId ownId = PlayerId.ALL.get(StringSerializer.deserializeInt(splittedCommand[1]));
							localPlayer.setPlayers(ownId, playerNames);
							break;
							
						case TRMP:
							localPlayer.setTrump(Card.Color.ALL.get(StringSerializer.deserializeInt(splittedCommand[1])));
							break;
							
						case HAND:
							CardSet hand = CardSet.ofPacked(StringSerializer.deserializeLong(splittedCommand[1]));
							localPlayer.updateHand(hand);
							break;
							
						case TRCK:
							Trick trick = Trick.ofPacked(StringSerializer.deserializeInt(splittedCommand[1]));
							localPlayer.updateTrick(trick);
							break;
							
						case CARD:
							String[] args = StringSerializer.split(",", splittedCommand[1]);
							
							// constructs the turnState with the 3-packed 
							long pkScore = StringSerializer.deserializeLong(args[0]);
							long pkUnplayedCards = StringSerializer.deserializeLong(args[1]);
							int pkTrick = StringSerializer.deserializeInt(args[2]);
							
							TurnState state = TurnState.ofPackedComponents(pkScore, pkUnplayedCards, pkTrick);
							CardSet handCard = CardSet.ofPacked(StringSerializer.deserializeLong(splittedCommand[2]));
							
							String cardToReturn = StringSerializer.serializeInt(localPlayer.cardToPlay(state, handCard).packed());
							w.write(cardToReturn+"\n");
							w.flush();
							break;
							
						case SCOR:
							Score score = Score.ofPacked(StringSerializer.deserializeLong(splittedCommand[1]));
							localPlayer.updateScore(score);
							break;
							
						case WINR:
							TeamId team = TeamId.ALL.get(StringSerializer.deserializeInt(splittedCommand[1]));
							localPlayer.setWinningTeam(team);
							break;
						
						case CHTR:
							CardSet set = CardSet.ofPacked(StringSerializer.deserializeLong(splittedCommand[1]));
							Color trump = localPlayer.chooseTrump(set);
							String trumpToReturn = StringSerializer.serializeInt(trump.ordinal());
							w.write(trumpToReturn+"\n");
							w.flush();
							break;
							
						case FRST:
							PlayerId player = PlayerId.ALL.get(StringSerializer.deserializeInt(splittedCommand[1]));
							localPlayer.setFirstPlayer(player);
							break;
							
						// default in case of a wrong instruction
						default :
							throw new Error("Command not found");
						}
						command = r.readLine();
					}
					
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
	}
}
