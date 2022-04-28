package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.awt.*;
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

import static java.lang.Math.max;
import static java.lang.Math.min;

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
		Minimax m = new Minimax();
		int depth = 2;
		m.constructTree(0, depth, (Board.GameState) board);
		Move bestMove = m.findBestNode(m.tree.getRoot(), depth, true, board).move;
		return bestMove;
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
		//System.out.println(maxEntry.getValue());
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

	private ImmutableSet<Piece> getDetectives(Board board) {
		ImmutableSet.Builder<Piece> detectivesBuilder = ImmutableSet.builder();
		for (Piece piece : board.getPlayers()) {
			if (piece.isDetective()) {
				detectivesBuilder.add(piece);
			}
		}
		return detectivesBuilder.build();
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
		int destination = move.accept(new Move.FunctionalVisitor<>((singleMove -> singleMove.destination),
					doubleMove -> doubleMove.destination2));
		int score = scoreDistanceDetectives(board, destination);
		/*if (losingMove(board, destination)) {
			System.out.println("losingmove");
			return 0;
		} */
		return score;
	}

	public class Minimax {
		Tree tree;

		private class Node {
			private Move move;
			private int score;
			private int data = 0;
			private Board.GameState gameState;
			private List<Node> children;

			private Node(Move move, int score, Board.GameState gameState) {
				this.move = move;
				this.score = score;
				this.gameState = gameState;
				this.children = List.of();
			}

			public Node() {}

			public Move getMove() {
				return this.move;
			}

			public int getScore() {
				return this.score;
			}

			public void setData(int data) { this.data = data;}

			public Board.GameState gameState() {
				return this.gameState;
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

		private class Tree {
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

		private void constructTree(int score, int depth, Board board) {
			tree = new Tree();
			Node root = new Node(null, score, (Board.GameState) board);
			tree.setRoot(root);
			constructTree(root, depth, (Board.GameState) board);
		}

		private void constructTree(Node parentNode, int depth, Board.GameState gameState) {
			/* System.out.println("moves:" + gameState.getAvailableMoves().size());
			System.out.println(gameState.getAvailableMoves());
			System.out.println(depth); */

			if (depth == 0) {
				System.out.println("x\n");
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(move, score(move, gameState), gameState);
					parentNode.addChildren(newNode);
				}
			}
			else {
				for (Move move : gameState.getAvailableMoves()) {
					System.out.println("move:" + move);
					Node newNode = new Node(move, score(move, gameState), gameState);
					parentNode.addChildren(newNode);
					System.out.println(newNode.getScore() + "\n");
					Board.GameState newGameState = gameState.advance(move);
					constructTree(newNode, depth - 1, newGameState);
				}
			}
		}

		public Node findBestNode(Node node, int depth, boolean isMaxPlayer, Board board) {
			int bestData = minimax(node, depth, isMaxPlayer, board).right();
			for (Node child : node.getChildren()) {
				if (child.data == bestData) {
					return child;
				}
			}
			return null;
		}

		public Pair<Node, Integer> minimax(Node node, int depth, boolean isMaxPlayer, Board board) {
			if (depth == 0 || !board.getWinner().isEmpty()) {
				return Pair.pair(node, node.getScore());
			}
			if (isMaxPlayer) {
				int maxEval = -100000;
				Node maxNode = node;
				for (Node child : node.getChildren()) {
					Pair<Node, Integer> eval = minimax(child, depth - 1, false, board);
					if (eval.right() > maxEval) {
						maxEval = eval.right();
						maxNode = eval.left();
						child.data = eval.right();
					}
				}
				return Pair.pair(maxNode, maxEval);
			}
			else {
				int minEval = 100000;
				Node minNode = node;
				for (Node child : node.getChildren()) {
					Pair<Node, Integer> eval = minimax(child, depth - 1, true, board);
					if (eval.right() < minEval) {
						minEval = eval.right();
						minNode = eval.left();
						child.data = eval.right();
					}
				}
				return Pair.pair(minNode, minEval);
			}
		}
	}
}




