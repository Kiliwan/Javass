
package ch.epfl.javass.jass;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits64;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class PackedScore {
	
	private PackedScore() {}
	
	public static final long INITIAL = 0L;
	
	private static final int TURN_POINTS_SIZE = 9;
	private static final int GAME_POINTS_SIZE = 11;
	private static final int POINTS_PER_TURN = 157;
	private static final int TRICK_SIZE = 4;

	
	
	/**
	 * Verify that the given Long value corresponds to a valid "packed" score
	 * @param pkScore		"packed" score given
	 * @return				true if valid, false otherwise
	 */
	public static boolean isValid(long pkScore) {
		// the value is first separated in the two scores from each team
		return computeValid(pkScore, TeamId.TEAM_1) && computeValid(pkScore, TeamId.TEAM_2);
	}
	private static boolean computeValid(long teamScore, TeamId team) {
		long score = Bits64.extract(teamScore, team.ordinal() * Integer.SIZE, Integer.SIZE);
		int sizeMax = TRICK_SIZE + TURN_POINTS_SIZE + GAME_POINTS_SIZE;
		
		if(teamScore  < INITIAL || score >= (1L << sizeMax)) {
            return false;
        }
        boolean tricks = Bits64.extract(score, 0, TRICK_SIZE) <= Jass.TRICKS_PER_TURN;
        boolean turn = Bits64.extract(score, TRICK_SIZE, TURN_POINTS_SIZE) <= POINTS_PER_TURN + Jass.MATCH_ADDITIONAL_POINTS;
        boolean game = Bits64.extract(score, TRICK_SIZE + TURN_POINTS_SIZE, GAME_POINTS_SIZE) <= Jass.WINNING_POINTS * 2; // winning points are 2000 according to the given instructions
        
        return tricks && turn && game;
	}
	
	/**
	 * Method to add the points of the melds of a given player to the score of the turn of its team
	 * @param pkScore	"packed" score value
	 * @param melds		set of melds
	 * @param team		team to which the melds belong to
	 * @return			new Score with the points of the meld added to the turn points
	 */
	public static long meldPoints(long pkScore, MeldSet melds, TeamId team) {
		if(team.equals(TeamId.TEAM_1)) {
		return pack(PackedScore.turnTricks(pkScore, TeamId.TEAM_1),
					PackedScore.turnPoints(pkScore, TeamId.TEAM_1), 
					PackedScore.gamePoints(pkScore, TeamId.TEAM_1) + melds.points(),
					PackedScore.turnTricks(pkScore, TeamId.TEAM_2),
					PackedScore.turnPoints(pkScore, TeamId.TEAM_2),
					PackedScore.gamePoints(pkScore, TeamId.TEAM_2));
		} else {
			return pack(PackedScore.turnTricks(pkScore, TeamId.TEAM_1),
					PackedScore.turnPoints(pkScore, TeamId.TEAM_1),
					PackedScore.gamePoints(pkScore, TeamId.TEAM_1),
					PackedScore.turnTricks(pkScore, TeamId.TEAM_2),
					PackedScore.turnPoints(pkScore, TeamId.TEAM_2),
					PackedScore.gamePoints(pkScore, TeamId.TEAM_2) + melds.points());
		}	
	}
	
	/**
	 * Pack the different game values into a long "packed" score value
	 * @param turnTricks1		number of turn won by TEAM_1
	 * @param turnPoints1		number of points won by TEAM_1 in this turn
	 * @param gamePoints1		number of points won by TEAM_1 in the game
	 * @param turnTricks2		number of turn won by TEAM_2
	 * @param turnPoints2		number of points won by TEAM_2 in this turn
	 * @param gamePoints2		number of points won by TEAM_2 in the game
	 * @return					long "packed" score
	 */
	public static long pack(int turnTricks1, int turnPoints1, int gamePoints1,
							int turnTricks2, int turnPoints2, int gamePoints2) {
		
		long scoreTeam1 = computePack(turnTricks1, turnPoints1, gamePoints1);
		long scoreTeam2 = computePack(turnTricks2, turnPoints2, gamePoints2);

	    
	    return scoreTeam2 << Integer.SIZE | scoreTeam1; 			
	}
	
	public static long computePack(int turnTricks, int turnPoints, int gamePoints) {
	    boolean tricks = turnTricks <= Jass.TRICKS_PER_TURN;
        boolean turn = turnPoints <= POINTS_PER_TURN + Jass.MATCH_ADDITIONAL_POINTS;
        boolean game = gamePoints <= Jass.WINNING_POINTS * 2;
        
        assert(tricks && turn && game);
        
        return PackedScore.INITIAL | (long) gamePoints << TRICK_SIZE + TURN_POINTS_SIZE | 
									 (long) turnPoints << TRICK_SIZE | 
									 (long) turnTricks;			 
	}
	
	/**
	 * Extract the number of tricks won by the given team if valid
	 * @param pkScore		"packed" score
	 * @param t				given team
	 * @return				integer of number of turn won by given team
	 */
	public static int turnTricks(long pkScore, TeamId t) {
		assert(isValid(pkScore) &&
			  (t == TeamId.TEAM_1 || t == TeamId.TEAM_2));
		
		return (int) Bits64.extract(pkScore, Integer.SIZE * t.ordinal(), TRICK_SIZE);

	}
	
	/**
	 * Extract the number of points won by the given team in the turn if valid
	 * @param pkScore		"packed" score
	 * @param t				given team
	 * @return				integer of number of points won by given team in this turn
	 */
	public static int turnPoints(long pkScore, TeamId t) {
		assert(isValid(pkScore) &&
			  (t == TeamId.TEAM_1 || t == TeamId.TEAM_2));
		
		return (int) Bits64.extract(pkScore, Integer.SIZE * t.ordinal() + TRICK_SIZE, TURN_POINTS_SIZE);
	}

	/**
	 * Extract the number of points won by the given team in the game if valid
	 * @param pkScore		"packed" score
	 * @param t				given team
	 * @return				integer of number of points won by given team in the game
	 */
	public static int gamePoints(long pkScore, TeamId t) {
		assert(isValid(pkScore) &&
			  (t == TeamId.TEAM_1 || t == TeamId.TEAM_2));
		 
		return (int) Bits64.extract(pkScore, Integer.SIZE * t.ordinal() + TRICK_SIZE + TURN_POINTS_SIZE, GAME_POINTS_SIZE);
	}
	
	/**
	 * Return the total points of the given team
	 * @param pkScore		"packed" score
	 * @param t				given team
	 * @return				integer of total number of points won by given team
	 */
	public static int totalPoints(long pkScore, TeamId t) {
		assert(isValid(pkScore) &&
              (t == TeamId.TEAM_1 || t == TeamId.TEAM_2));
		
		return gamePoints(pkScore, t) + turnPoints(pkScore, t);
	}
	
	/**
	 * Re-packs the score according to the points of the last trick won, add 100 points if one team won all the tricks in this turn
	 * @param pkScore			"packed" score
	 * @param winningTeam		team who won last turn
	 * @param trickPoints		points of the last turn
	 * @return					new updated "packed" score
	 */
	public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
		assert(isValid(pkScore) &&
			  (winningTeam == TeamId.TEAM_1 || winningTeam == TeamId.TEAM_2));

		
		int modifiedScore = trickPoints;
		
		if(turnTricks(pkScore, winningTeam) + 1 == Jass.TRICKS_PER_TURN) {
			modifiedScore += Jass.MATCH_ADDITIONAL_POINTS;
		}
		int trick1 = turnTricks(pkScore, TeamId.TEAM_1);
		int turn1 = turnPoints(pkScore, TeamId.TEAM_1);
		int trick2 = turnTricks(pkScore, TeamId.TEAM_2);
		int turn2 = turnPoints(pkScore, TeamId.TEAM_2);
		
		if (winningTeam == TeamId.TEAM_1) {
			++trick1;
			turn1 += modifiedScore;
			
		} else {
			++trick2;
			turn2 += modifiedScore;
		}
		
		return pack(trick1, 
				    turn1, 
				    gamePoints(pkScore, TeamId.TEAM_1),
			        trick2, 
			        turn2, 
			        gamePoints(pkScore, TeamId.TEAM_2));
	}
	
	/**
	 * Re-packs the score according to the points of the last turn, restores the number of tricks and turn points for a new turn
	 * @param pkScore		"packed" score
	 * @return				new updated "packed" score
	 */
	public static long nextTurn(long pkScore) {
		assert(isValid(pkScore) &&
			  (PackedScore.turnTricks(pkScore, TeamId.TEAM_1) + PackedScore.turnTricks(pkScore, TeamId.TEAM_2) == 9));
		
		return pack(0, 
				    0, 
				    gamePoints(pkScore, TeamId.TEAM_1) + turnPoints(pkScore, TeamId.TEAM_1), 
				    0, 
				    0, 
				    gamePoints(pkScore, TeamId.TEAM_2) + turnPoints(pkScore, TeamId.TEAM_2));
	}
	
	/**
	 * Prints the current state ot the score in the format (team1)/(team2) : (tricks_won, turn_points, game_points)
	 */
	public static String toString(long pkScore) {
		assert(isValid(pkScore));
		
        StringJoiner j = new StringJoiner(",", "{", "}");
        
        for(TeamId t : TeamId.ALL) {
        	j.add(Integer.toString(turnTricks(pkScore, t)))
             .add(Integer.toString(turnPoints(pkScore, t)))
             .add(Integer.toString(gamePoints(pkScore, t)));
        }
		return j.toString();
	}
}
