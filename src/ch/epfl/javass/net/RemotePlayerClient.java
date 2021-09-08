package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */ 
public class RemotePlayerClient implements Player, AutoCloseable {
	
	private final Socket socket;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	
	/**
	 * Constructs a new instance Client
	 * @param host	the given host which the client will connect   
	 */
	public RemotePlayerClient(String host) throws IOException {
		this.socket = new Socket(host, RemotePlayerServer.PORT);
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),US_ASCII));
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),US_ASCII));
	}
	
	@Override
	public void updateMelds(Map<PlayerId, String> playerHands) {
		List<String> encodedMelds = new ArrayList<>();
		for(int i = 0; i < PlayerId.COUNT; ++i)  {
			encodedMelds.add(StringSerializer.serializeString(playerHands.get(PlayerId.ALL.get(i))));
		}
		
		String hands = StringSerializer.combine(",", encodedMelds.toArray((new String[encodedMelds.size()])));
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.MELD.name(), hands});
		
		send(toSend);
	}
	
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		
		String encodedId = StringSerializer.serializeInt(ownId.ordinal());
		
		List<String> encodedNames = new ArrayList<>();
		for(int i = 0; i < PlayerId.COUNT; ++i)  {
			encodedNames.add(StringSerializer.serializeString(playerNames.get(PlayerId.ALL.get(i))));
		}
		String names = StringSerializer.combine(",", encodedNames.toArray((new String[encodedNames.size()])));
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.PLRS.name(), encodedId, names});

		send(toSend);
	}
	
	@Override
	public void updateHand(CardSet newHand) {
		
		String encodedHand = StringSerializer.serializeLong(newHand.packed());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.HAND.name(), encodedHand});

		send(toSend);
	}
	
	@Override
	public void setTrump(Color trump) {
		
		String encodedTrump = StringSerializer.serializeInt(trump.ordinal());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.TRMP.name(), encodedTrump});

		send(toSend);
	}
	
	@Override
	public void updateTrick(Trick newTrick) {
		
		String encodedTrick = StringSerializer.serializeInt(newTrick.packed());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.TRCK.name(), encodedTrick});
		
		send(toSend);
	}
	
	@Override
	public void updateScore(Score score) {
		
		String encodedScore = StringSerializer.serializeLong(score.packed());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.SCOR.name(), encodedScore});
		
		send(toSend);
	}
	
	@Override
	public void setWinningTeam(TeamId winningTeam) {
		
		String encodedTeam = StringSerializer.serializeInt(winningTeam.ordinal());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.WINR.name(), encodedTeam});
		
		send(toSend);
	}
	
	
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		
		String encodedScore = StringSerializer.serializeLong(state.packedScore());
		String encodedUnplayedCards = StringSerializer.serializeLong(state.packedUnplayedCards());
		String encodedTrick = StringSerializer.serializeInt(state.packedTrick());
		
		// combines the score, the unplayed cards and the trick to create the encoded turnState
		String encodedTurnState = StringSerializer.combine(",", new String[]{encodedScore, encodedUnplayedCards, encodedTrick});
		String encodedHand = StringSerializer.serializeLong(hand.packed());
		
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.CARD.name(), encodedTurnState, encodedHand});

		send(toSend);
		
		try {
			Card card = Card.ofPacked(StringSerializer.deserializeInt(reader.readLine()));
			return card;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public Color chooseTrump(CardSet hand) {
		String encodedHand = StringSerializer.serializeLong(hand.packed());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.CHTR.name(), encodedHand});
		send(toSend);
		
		try {
			Color trump = Color.ALL.get(StringSerializer.deserializeInt(reader.readLine()));
			return trump;	
		} catch (IOException e) {
			throw new UncheckedIOException(e); 
		}
	}
	
	@Override
	public void setFirstPlayer(PlayerId player) {
		String encodedPlayer = StringSerializer.serializeInt(player.ordinal());
		String toSend = StringSerializer.combine(" ", new String[]{JassCommand.FRST.name(), encodedPlayer});
		send(toSend);
	}
	
	/*
	 * Sends the wanted message to the server
	 */
	private void send(String toSend) {
		try {
			writer.write(toSend +"\n");
			writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public void close() throws Exception {
		reader.close();
		writer.close();
		socket.close();
	}
}