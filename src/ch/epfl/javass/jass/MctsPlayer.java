package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357) 
 */
public final class MctsPlayer implements Player {
	
	private final PlayerId ownId;
	private final SplittableRandom srng;
	private final int iterations;
	
	/**
	 * Constructor of a MctsPlayer (simulated player)
	 * @param ownId			PlayerId of the simulated player
	 * @param rngSeed		seed for the random values
	 * @param iterations	number of simulations that the simulated player will do before playing a card
	 */
	public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
		Preconditions.checkArgument(iterations >= Jass.MINIMUM_ITERATIONS);
		
		this.ownId = ownId;
		this.srng = new SplittableRandom(rngSeed);
		this.iterations = iterations;
	}
	
	/**
	 * Calculates the score of a particular child node
	 * @param child			child node to calculate the score of
	 * @param curiosity		curiosity factor of the calculation
	 * @param parent		parent node of the child node
	 * @return double		score of the child node
	 */
	private static double bestFunction(Node child, int curiosity, Node parent) {

		if(child == null) { // always go for empty child
			return Double.POSITIVE_INFINITY;
		} else {
			double nodeScore = child.points / child.computed;
			double intermediate = 2 * Math.log(parent.computed) / child.computed;
			double curiosityScore = curiosity * Math.sqrt(intermediate);
			return nodeScore + curiosityScore;
		}
	}
	
	/**
	 * Simulates a turn from the given state and returns the Score instance of it
	 * @param state			state from which we want to simulate the end of turn
	 * @param mctsHand		hand from the simulated player
	 * @param root			root node
	 * @return Score		Score instance of the simulated turn
	 */
	private Score getRandomScore(TurnState state, CardSet mctsHand, Node root) {
		
		while(!state.isTerminal()) {
			CardSet playable = root.playableCards(state, mctsHand, state.nextPlayer(), this.ownId);
			
			Card cardToPlay = playable.get(srng.nextInt(playable.size()));
			state = state.withNewCardPlayedAndTrickCollected(cardToPlay);
		}
		return state.score();
	}
	
	@Override
	public Color chooseTrump(CardSet hand) {
		
		List<CardSet> sets = new ArrayList<>();
		
		for(Color c : Color.ALL) {
			if(hand.subsetOfColor(c).size() !=0) {
				sets.add(hand.subsetOfColor(c));
			}
		}
		
		for(int i = 1; i < sets.size(); ++i) {
			if(sets.get(i).size() <sets.get(i-1).size() - 2) {
				sets.remove(i);
			} 
		}
		if(sets.size() == 1) {
			return sets.get(0).get(0).color();
		}		
		
		Card bestC = null, bestOfSet = null;
		for(CardSet set : sets) {
			for(int j = 0; j < set.size(); ++j) {
				if(bestOfSet == null || set.get(j).rank().trumpOrdinal() > bestOfSet.rank().trumpOrdinal()) {
					bestOfSet = set.get(j);
				}
			}
			if(bestC == null || bestOfSet.rank().trumpOrdinal() > bestC.rank().trumpOrdinal()) {
				bestC = bestOfSet;
			}
		}	
		return bestC.color();
	} 
	
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		
		// choice of cards
		CardSet playableHand = state.trick().playableCards(hand);
		
		// if last card to play
		if(playableHand.size() == 1) {
			return playableHand.get(0);
		}
		
		// root creation, node at the base of the tree
		Node root = new Node(state, playableHand, null, ownId, ownId);
		
		// main loop
		for(int i = 0; i < iterations; i++) {
			
			// select best end, returns path
			List<Node> path = root.selected();
			
			// get last from path, current
			Node current = path.get(path.size() - 1);
			
			// add child to end of path, returns child, if not possible returns end
			Node child = current.addChild(hand, ownId);
			path.add(child);

			// simulate score
			Score randomScore = getRandomScore(child.nodeState, hand, root);

			// update scores
			child.update(path, randomScore, ownId);
		}
		
		int best = 0;
		for(int i = 0; i < root.childNodes.length; i++) {
			if(bestFunction(root.childNodes[i], 0, root) > bestFunction(root.childNodes[best], 0, root) ) {
				best = i;
			}
		} // choose best child node of the root, best of the card from the hand of the simulated player
		
		return root.childNodes[best].card;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private static class Node{
		
		private final int CURIOSITY = 40; // curiosity value of the simulation, the higher it is, the less conservative and more curious the simulation will be
		
		private TurnState nodeState;
		private Node[] childNodes;
		private CardSet remainingLinks;
		private double points;
		private int computed;
		private Card card;
		
		/**
		 * Constructor of the Node used for the Monte Carlo simulation tree
		 * @param state			TurnState of the node, containing the Score, UnplayedCards and Trick
		 * @param hand			Cards with which the simulation can go further down this node
		 * @param card			Card that this node represents
		 * @param nextPlayer	next player to play after this node
		 * @param mctsId		PlayerId of the simulated player
		 */
		public Node(TurnState state, CardSet hand, Card card, PlayerId nextPlayer, PlayerId mctsId) {
			
			this.nodeState = state;
			
			if(state.isTerminal()) { // when state is terminal, cannot further develop down this node
				this.remainingLinks = CardSet.EMPTY;
				this.childNodes = new Node[0];
			} else {
				this.remainingLinks = playableCards(state, hand, nextPlayer, mctsId);
				this.childNodes = new Node[remainingLinks.size()];
			}
			
			this.card = card;
		}
		
		/**
		 * Defines which cards are playable according to the current situation
		 * @param state			current TurnState
		 * @param hand			current hand available
		 * @param nextPlayer	player playing now
		 * @param mctsId		PlayerId of the simulated player
		 * @return CardSet		playable cards
		 */
		private CardSet playableCards(TurnState state, CardSet hand, PlayerId nextPlayer, PlayerId mctsId) {
			CardSet playable;
			
			if(nextPlayer == mctsId) {
				playable = state.trick().playableCards(state.unplayedCards().intersection(hand));
			} else {
				playable = state.trick().playableCards(state.unplayedCards().difference(hand));
			}
			return playable;
		}
		
		/**
		 * Selects the best node to simulate more according to their respective scores
		 * @return List<Node>	list of nodes from the root node (included, first position) to the node to the expand, (included, last position)
		 */
		private List<Node> selected(){
			
			Node toFollow = this;
			
			List<Node> path = new ArrayList<>();
			path.add(toFollow);
			
			// if can go further down this node and all children are full
			while(!toFollow.remainingLinks.isEmpty() && toFollow.childsFull()) {
				
				// selecting where to continue
				Node best = toFollow.childNodes[0];
				for(Node n : toFollow.childNodes) {
					if(bestFunction(n, CURIOSITY, this) > bestFunction(best, CURIOSITY, this)) {
						best = n;
					}
				}
				
				toFollow = best;
				// add it to the node before going further
				path.add(toFollow);
			}
			// otherwise, we are at the node to expand
			return path;
		}
		
		/**
		 * Returns true if all children are full
		 */
		private boolean childsFull() {
			for(int i = 0; i < childNodes.length; i++) {
				if(childNodes[i] == null) {
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Adds a new child in the children nodes of the given node and returns it
		 * @param mctsHand		hand of the simulated player
		 * @param mctsId		PlayerId of the simulated player
		 * @return Node			child Node just created
		 */
		private Node addChild(CardSet mctsHand, PlayerId mctsId) {
			
			for(int i = 0; i < childNodes.length; i++) {
				if(childNodes[i] == null) {
					// card correspond to the position in the childNodes
					Card childCard = remainingLinks.get(i);
					TurnState childState = nodeState.withNewCardPlayedAndTrickCollected(childCard);
					
					PlayerId nextPlayer;
					if(childState.packedTrick()==PackedTrick.INVALID) {
						nextPlayer = this.nodeState.trick().winningPlayer();
					} else {
						nextPlayer = childState.nextPlayer();
					}
					
					childNodes[i] = new Node(childState, mctsHand, childCard, nextPlayer, mctsId);
					return childNodes[i];
				}
			}
			// if we are at the end of a branch, returns itself so it will re-simulate once more
			return this;
		}
		
		/**
		 * Updates all the scores of the concerned nodes according to the new simulated Score
		 * @param path			list of all the nodes that should be updated, from the new child up to the root node
		 * @param randomScore	random simulated Score
		 * @param mctsId		PlayerId of the simulated player
		 */
		private void update(List<Node> path, Score randomScore, PlayerId mctsId) {
			for(int i = 0; i < path.size(); i++) {
				Node toUpdate = path.get(i);
				
				toUpdate.points += (double)randomScore.turnPoints(mctsId.team());
				
				toUpdate.computed++;
			}
		}
	}
}