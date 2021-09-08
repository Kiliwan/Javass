package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class Score {
	
	private long score;
	public static final Score INITIAL = ofPacked(PackedScore.INITIAL);
	private static final int POINTS_PER_TURN = 157;
	
	/**
	 * Constructor of Score; throws Exception if given "packed" score is invalid
	 * @param packed	"packed" long representing scores of the game
	 */
	private Score(long packed) {
		this.score = packed;
	}
	/**
	 * Method used to construct a new instance of score so that the constructor can in itself stay private
	 * @param packed					"packed" long representing scores of the game
	 * @throws IllegalArgumentException if the packed score is incorrect	
	 * @return Score 					object containing the "packed" score as an attribute

	 */
	public static Score ofPacked(long packed) {
		Preconditions.checkArgument(PackedScore.isValid(packed));
		
		return new Score(packed);
	}
	
	/**
	 * Returns new score with the points of the given set of melds added to the given team
	 * @param packed	"packed" long representing scores of the game
	 * @param melds		set of melds
	 * @param team		team to whick the points of the set of melds belong
	 */
	public Score meldPoints(MeldSet melds, TeamId team) {
		return new Score(PackedScore.meldPoints(packed(), melds, team));
	}
	
	/**
	 * Returns the "packed" integer of the score contained in this instance
	 */
	public long packed() {
		return this.score;
	}
	
	/**
	 * Returns the tricks won by the given team in this turn
	 * @param t 	given team
	 */
	public int turnTricks(TeamId t) {
		
		//validity of the teamId done in the "packed" method
		return PackedScore.turnTricks(this.score, t);
	}
	
	/**
	 * Returns the number of points won by the given team in this turn
	 * @param t 	given team
	 */
	public int turnPoints(TeamId t) {
		
		//validity of the teamId done in the "packed" method
		return PackedScore.turnPoints(this.score, t);
	}

	/**
	 * Returns the number of points won by the given team in this game without the actual turn
	 * @param t 	given team
	 */
	public int gamePoints(TeamId t) {
		
		//validity of the teamId done in the "packed" method
		return PackedScore.gamePoints(this.score, t);
	}

	/**
	 * Returns the total number of points won by the given team in this game
	 * @param t		given team
	 */
	public int totalPoints(TeamId t) {
		
		//validity of the teamId done in the "packed" method
		return PackedScore.totalPoints(this.score, t);
	}

	/**
	 * Returns a new actualized score according to the last trick won
	 * @param winningTeam				team who won last trick
	 * @param trickPoints				points of the last trick
	 * @throws IllegalArgumentException if the given number of points is less than 0 or greater than the maximum of points per turn
	 * @return Score					new Score with the points of the previous trick
	 */
	public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
		Preconditions.checkIndex(trickPoints, POINTS_PER_TURN); // because the "match" points are handled later in this method

		//validity of the teamId done in the "packed" method
		return new Score(PackedScore.withAdditionalTrick(this.score, winningTeam, trickPoints));
	
	}
	
	/**
	 * Returns a new actualized score of the end of the turn
	 * @return		new "packed" score
	 */
	public Score nextTurn() {
		
		long nextTurn = PackedScore.nextTurn(this.score);
		return new Score(nextTurn);
	}
	
	@Override
	public boolean equals(Object other) {
	    
        if(!(other instanceof Score)) {
            return false;
        }
        return this.score == ((Score)other).packed();  
    }
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override // format (tricks,turn,game) for (team1)/(team2)
	public String toString() {
		
		//validity of the "packed" score done in the packed method
		return PackedScore.toString(this.score);
	}
	
}
