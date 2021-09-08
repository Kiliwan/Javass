package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class TrickBean {
	
	private ObjectProperty<Color> trump;
	private MapProperty<PlayerId, Card> trick;
	private ObjectProperty<PlayerId> winningPlayer;
	private ObjectProperty<PlayerId> firstPlayer;
	
	/**
	 * Constructor for the JavaFX bean representing the trick being played
	 */
	public TrickBean() {
		this.trump = new SimpleObjectProperty<Color>();
		this.trick = new SimpleMapProperty<PlayerId, Card>();
		this.winningPlayer = new SimpleObjectProperty<PlayerId>();
		this.firstPlayer = new SimpleObjectProperty<PlayerId>();
	}
	
	/**
	 * Getter for the trump property of the trick bean
	 */
	public ReadOnlyObjectProperty<Color> trumpProperty(){
		return trump;
	}
	
	/**
	 * Setter for the trump property of the trick bean
	 * @param trump		trump color to set
	 */
	public void setTrumpProperty(Color trump) {
		this.trump.set(trump);
	}
	
	public ReadOnlyObjectProperty<PlayerId> firstProperty(){
		return firstPlayer;
	}
	
	public void setFirstProperty(PlayerId player) {
		this.firstPlayer.set(player);
	}
	
	/**
	 * Getter for the trick property of the trick bean
	 */
	public ObservableMap<PlayerId, Card> trickProperty(){
		return FXCollections.unmodifiableObservableMap(trick);
	}
	
	/**
	 * Setter for the trick property of the trick bean
	 * @param newTrick		new trick to set
	 */
	public void setTrickProperty(Trick newTrick) {
		ObservableMap<PlayerId, Card> trick = FXCollections.observableHashMap();
		
		for(int i = 0; i < newTrick.size(); ++i) {
			trick.put(newTrick.player(i), newTrick.card(i));
		}
		trick = FXCollections.unmodifiableObservableMap(trick);
		if(newTrick.isEmpty()) {
			this.winningPlayer.set(null); // because if empty throws IllegalStateException
		} else {
			this.winningPlayer.set(newTrick.winningPlayer());

		}
		this.trick.set(trick);
	}
	
	/**
	 * Getter for the winningPlayer property of the trick bean
	 */
	public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty(){
		return winningPlayer;
	}
}
