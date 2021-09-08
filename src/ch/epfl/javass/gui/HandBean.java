package ch.epfl.javass.gui;


import java.util.Map;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class HandBean {
	
	private ListProperty<Card> hand;
	private SetProperty<Card> playableCards;
	private MapProperty<PlayerId, String> melds;
	
	/**
	 * Constructor for the JavaFX bean representing the hand of the player
	 */
	public HandBean() {
		this.hand = new SimpleListProperty<Card>(FXCollections.observableArrayList(null, null, null, null, null, null, null, null, null));
		this.playableCards = new SimpleSetProperty<Card>(FXCollections.observableSet());
		this.melds = new SimpleMapProperty<>(FXCollections.observableHashMap());
	}
	
	/**
	 * Getter for the melds property of the hand bean
	 */
	public ObservableMap<PlayerId, String> meldsProperty(){
		return FXCollections.unmodifiableObservableMap(melds);
		
	}
	
	/**
	 * Setter for the melds property of the hand bean
	 * @param melds		new melds to set
	 */
	public void setMelds(Map<PlayerId, String> melds) {
		ObservableMap<PlayerId, String> newMeld = FXCollections.observableMap(melds);
		
		for(PlayerId p : melds.keySet()) {
			newMeld.put(p, melds.get(p).toString());
		}
		newMeld = FXCollections.unmodifiableObservableMap(newMeld);
		this.melds.set(newMeld);
	}
	
	/**
	 * Getter for the hand property of the hand bean
	 */
	public ObservableList<Card> handProperty(){
		return FXCollections.unmodifiableObservableList(hand);
	}
	
	/**
	 * Setter for the hand property of the hand bean
	 * @param newHand	new hand to set
	 */
	public void setHand(CardSet newHand) {	
		
		if(newHand.size()==Jass.HAND_SIZE) {
			for(int i=0;i<newHand.size();i++) {
				this.hand.set(i, newHand.get(i));
			}
			
		} else {

			for(int i = 0; i < this.hand.size(); ++i) {
				if(this.hand.get(i) != null && !newHand.contains(this.hand.get(i))) {
					this.hand.set(i, null);
				}
			}
		}
	}
	
	/**
	 * Getter for the playableCards property of the hand bean
	 */
	public ObservableSet<Card> playableCardsProperty(){
		return FXCollections.unmodifiableObservableSet(playableCards);
	}
	
	/**
	 * Setter for the playableCards property of the hand bean
	 * @param newPlayableCards	new playableCards to set
	 */
	public void setPlayableCards(CardSet newPlayableCards) {
		this.playableCards.get().clear();
		for(int i = 0; i < newPlayableCards.size(); ++i) {
			this.playableCards.add(newPlayableCards.get(i));
		}
	}
}
