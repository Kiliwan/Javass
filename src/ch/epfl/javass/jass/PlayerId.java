package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Enumeration for each player of the game 
 */
public enum PlayerId {
	
	PLAYER_1,
	PLAYER_2,
	PLAYER_3,
	PLAYER_4;
	
	public static final List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int COUNT = ALL.size();
	
	/**
	 * Return the team of the player from where the method is called
	 */
	public TeamId team() {
		return TeamId.ALL.get(this.ordinal() % 2);
	}
}
