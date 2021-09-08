package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.jass.TeamId;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 * Enumeration for each team of the game
 */
public enum TeamId {
	
	TEAM_1,
	TEAM_2;
	
	public static final List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int COUNT = ALL.size();
	
	/**
	 * Returns the other team than the one from where the method is called
	 */
	public TeamId other() {
		return TeamId.ALL.get((this.ordinal() + 1) % 2);
	}
}
