package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class ScoreBean {
	
	private IntegerProperty turnPoints1;
	private IntegerProperty turnPoints2;
	private IntegerProperty gamePoints1;
	private IntegerProperty gamePoints2;
	private IntegerProperty totalPoints1;
	private IntegerProperty totalPoints2;
	private ObjectProperty<TeamId> winningTeam;
	
	/**
	 * Constructor for the JavaFx bean representing the scores of the game
	 */
	public ScoreBean() {
		this.turnPoints1 = new SimpleIntegerProperty(0);
		this.turnPoints2 = new SimpleIntegerProperty(0);

		this.totalPoints1 = new SimpleIntegerProperty(0);
		this.totalPoints2 = new SimpleIntegerProperty(0);

		this.gamePoints1 = new SimpleIntegerProperty(0);
		this.gamePoints2 = new SimpleIntegerProperty(0);

	}
	
	/**
	 * Getter for the turnPoints property of the score bean according to the given team
	 * @param team		given team
	 */
	public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
		if(team.equals(TeamId.TEAM_1)) {
			return turnPoints1;
		} else {
			return turnPoints2;
		}
	}
	
	/**
	 * Setter for the turnPoints property of the score bean according to the given team
	 * @param team				given team
	 * @param newTurnPoints		turnPoints to set
	 */
	public void setTurnPoints(TeamId team, int newTurnPoints) {
		if(team.equals(TeamId.TEAM_1)) {
			turnPoints1.set(newTurnPoints);
		} else {
			turnPoints2.set(newTurnPoints);
		}
	}
	
	/**
	 * Getter for the gamePoints property of the score bean according to the given team
	 * @param team		given team
	 */
	public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
		if(team.equals(TeamId.TEAM_1)) {
			return gamePoints1;
		} else {
			return gamePoints2;
		}
	}
	
	/**
	 * Setter for the gamePoints property of the score bean according to the given team
	 * @param team				given team
	 * @param newGamePoints		gamePoints to set
	 */
	public void setGamePoints(TeamId team, int newGamePoints) {
		if(team.equals(TeamId.TEAM_1)) {
			gamePoints1.set(newGamePoints);
		} else {
			gamePoints2.set(newGamePoints);
		}
	}
	
	/**
	 * Getter for the totalPoints property of the score bean according to the given team
	 * @param team		given team
	 */
	public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
		if(team.equals(TeamId.TEAM_1)) {
			return totalPoints1;
		} else {
			return totalPoints2;
		}
	}
	
	/**
	 * Setter for the totalPoints property of the score bean according to the given team
	 * @param team				given team
	 * @param newTotalPoints	totalPoints to set
	 */
	public void setTotalPoints(TeamId team, int newTotalPoints) {
		if(team.equals(TeamId.TEAM_1)) {
			totalPoints1.set(newTotalPoints);
		} else {
			totalPoints2.set(newTotalPoints);
		}
	}
	
	/**
	 * Getter for the winningTeam property of the score bean
	 */
	public ReadOnlyObjectProperty<TeamId> winningTeamProperty(){
		return winningTeam;
	}
	
	/**
	 * Setter for the winningTeam property of the score bean with the given team
	 * @param winningTeam	given team
	 */
	public void setWinningTeam(TeamId winningTeam) {
		this.winningTeam.set(winningTeam);
	}
	
}
