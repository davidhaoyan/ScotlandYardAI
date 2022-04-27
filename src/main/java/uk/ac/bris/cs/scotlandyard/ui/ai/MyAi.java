package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.sun.source.tree.Tree;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

public class MyAi implements Ai {

	@Nonnull
	@Override
	public String name() {
		return "Tronkus Nullch";
	}

	// returns move with the highest score
	@Nonnull
	@Override
	public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {
		ImmutableSet<Move> moves = board.getAvailableMoves();
		ImmutableMap<Move, Integer> movesMap = getMap(moves, board);
		return getMaxEntry(movesMap).getKey();
	}

	// returns mapping from a move to its score
	private ImmutableMap<Move, Integer> getMap(ImmutableSet<Move> moves, Board board) {
		ImmutableMap.Builder<Move, Integer> mapBuilder = ImmutableMap.builder();
		for (Move move : moves) {
			int score = score(move, board);
			mapBuilder.put(move, score);
		}
		return mapBuilder.build();
	}

	// returns entry in map with the highest score
	private Map.Entry<Move, Integer> getMaxEntry(ImmutableMap<Move, Integer> map) {
		Map.Entry<Move, Integer> maxEntry = null;
		for (Map.Entry<Move, Integer> entry : map.entrySet()) {
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
				maxEntry = entry;
			}
		}
		System.out.println(maxEntry.getValue());
		return maxEntry;
	}

	// returns set of detective locations
	private ImmutableSet<Optional<Integer>> getDetectiveLocations(Board board) {
		ImmutableSet.Builder<Optional<Integer>> detectivesLocationsBuilder = ImmutableSet.builder();
		for (Piece piece : board.getPlayers()) {
			if (piece.isDetective()) {
				detectivesLocationsBuilder.add(board.getDetectiveLocation((Piece.Detective) piece));
			}
		}
		return detectivesLocationsBuilder.build();
	}

	// returns distance between two nodes
	private int getDistance(Board board, int start, int end) {
		return BFS.BFS(board.getSetup().graph, start, end);
	}

	// scoring method which chooses the destination with the most adjacent nodes
	private int scoreAdjacentDetectives(Board board, int destination) {
		int nearbyNodes = board.getSetup().graph.adjacentNodes(destination).size();
		return nearbyNodes;
	}

	// scoring method which chooses the destination the furthest away from all detectives
	private int scoreDistanceDetectives(Board board, int destination) {
		int distance = 0;
		ImmutableSet<Optional<Integer>> detectiveLocations = getDetectiveLocations(board);
		for (Optional<Integer> detectiveLocation : detectiveLocations) {
			distance += getDistance(board, destination, detectiveLocation.get());
		}
		return distance;
	}

	// losingMove is true if the destination is adjacent to a detective
	private boolean losingMove(Board board, int destination) {
		for (int node : board.getSetup().graph.adjacentNodes(destination)) {
			boolean adjacentDetective = getDetectiveLocations(board).stream().anyMatch(detectiveLocation ->
					detectiveLocation.get().equals(node));
			if (adjacentDetective) return true;
		}
		return false;
	}


	// returns the score of a move based on the current board
	public int score(Move move, Board board) {
		assert (move.commencedBy() == Piece.MrX.MRX);
		int destination = move.accept(new Move.FunctionalVisitor<>((singleMove -> singleMove.destination),
				doubleMove -> doubleMove.destination2));
		int score = scoreDistanceDetectives(board, destination);
		if (losingMove(board, destination)) return 0;
		return score;
	}

	public class Minimax {
		Tree tree;

		private class Node {
			private int score;
			private boolean isMaxPlayer;
			private List<Node> children;

			private Node(int score, boolean isMaxPlayer) {
				this.score = score;
				this.isMaxPlayer = isMaxPlayer;
				this.children = List.of();
			};

			public void setScore(int score) {
				this.score = score;
			}

			public int getScore() {
				return this.score;
			}

			public boolean getIsMaxPlayer() {
				return isMaxPlayer;
			}

			public void addChildren(Node child) {
				List newChildren = new ArrayList<>(children);
				newChildren.add(child);
				children = newChildren;
			}

			public List<Node> getChildren() {
				return this.children;
			}
		}

		public class Tree {
			private Node root;

			private Tree() {};

			public void setRoot(Node root) {
				this.root = root;
			}

			public Node getRoot() {
				return this.root;
			}

			public List showChildren() {
				return this.root.getChildren();
			}
		}

		public Tree constructTree(int score, int depth, boolean isMrXsTurn, Board board) {
			tree = new Tree();
			Node root = new Node(score, true);
			tree.setRoot(root);
			return constructTree(root, depth, true, board);
		}

		public Tree constructTree(Node parentNode, int depth, boolean isMrXsTurn, Board board) {
			// if isMrXsTurn iterate mrXmoves
			// if !isMrXsTurn iterate detective moves colour by colour
			Board.GameState gameState = (Board.GameState) board;
			if (isMrXsTurn) {
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(score(move, gameState), true);
					parentNode.addChildren(newNode);
					if (depth > 0) {
						gameState = gameState.advance(move);
						isMrXsTurn = !move.commencedBy().equals(Piece.MrX.MRX); //
						constructTree(newNode, depth - 1, isMrXsTurn, board);
					}
				}
			}
			else {
				//iterate detective moves colour by colour
			}
			return tree;
		}

		// filter detectives moves
	}
}
	//BELOW IS MY ATTEMPT AT DIJKSTRAS TO FIND DISTANCE OF DETECTIVES FROM MRX

	/*
    plan is
    so for every possible move destination, have to use dijkstras to find the distance from the nearest detective
    do this for each destination
    then of each of the move options, pick the move that has the destination with the furthest distance from
    the nearest detective.
    ideally not near the edges

    ohhhh im trying to think of ideas but i keep on wanting to implement it the
    long way because it's easier to understand. My thought process is breaking.
    because i was to do my own scoring method but that's not what dijkstra wants
    me to do? I need such big brain juice im going to draw this out
    but im cracking it slowly

    dijkstras algorithm is for shortest length but i thought we are finding the longesy
    one?

	cast board to gamestate advance for minimax
	 */

/*
	public Graph
	public void dijkstra(Board board, Move move){
		for (node)

	}
}
*/




