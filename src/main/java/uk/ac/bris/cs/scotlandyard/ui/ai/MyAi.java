package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MyAi implements Ai {

	@Nonnull
	@Override
	public String name() {
		return "Kenneth";
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
		//System.out.println(maxEntry.getValue());
		return maxEntry;
	}

	// returns entry in map with the second highest score
	private Map.Entry<Move, Integer> getSecondMaxEntry(ImmutableMap<Move, Integer> map) {
		Map.Entry<Move, Integer> secondMaxEntry = null;
		for (Map.Entry<Move, Integer> entry : map.entrySet()) {
			if (secondMaxEntry == null || entry.getValue().compareTo(getMaxEntry(map).getValue()) < 0) {
				if (entry.getValue().compareTo(secondMaxEntry.getValue()) > 0)
				secondMaxEntry = entry;
			}
		}
		//System.out.println(maxEntry.getValue());
		return secondMaxEntry;
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

	private int getMrXLocation(Board board) {
		Board.GameState gameState = (Board.GameState) board;
		int source = gameState.getAvailableMoves().asList().get(0).source();
		return source;
	}


	// returns distance between two nodes
	private int getDistance(Board board, int start, int end) {
		return BFS.BFS(board.getSetup().graph, start, end);
	}

	// scoring method which chooses the destination with the most adjacent nodes
	private int scoreAdjacentNodes(Board board, int destination) {
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

	private boolean winningMove(Board board, int destination) {
		if (destination == getMrXLocation(board)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean uselessMove(int destination, Move move) {
		if (destination == move.source()) {
			return true;
		} else {
			return false;
		}
	}

	private Move bestIfSameMove(Move move1, Move move2){
		List< ScotlandYard.Ticket > ticketList1 =
				move1.accept(new Move.FunctionalVisitor<>((singleMove -> List.of(singleMove.ticket)),
								(doubleMove -> List.of(doubleMove.ticket1, doubleMove.ticket2))));
		List< ScotlandYard.Ticket > ticketList2 =
				move2.accept(new Move.FunctionalVisitor<>((singleMove -> List.of(singleMove.ticket)),
						(doubleMove -> List.of(doubleMove.ticket1, doubleMove.ticket2))));
		if (ticketList1.size() == 2 && ticketList2.size() == 1 ) return move2;
		if (ticketList2.size() == 2 && ticketList1.size() == 1 ) return move1;
		if (ticketList1.contains(ScotlandYard.Ticket.SECRET)) return move2;
		if (ticketList2.contains(ScotlandYard.Ticket.SECRET)) return move1;
		return null;
	}

	// returns the score of a move based on the current board
	public int score(Move move, Board board) {
		int destination = move.accept(new Move.FunctionalVisitor<>((singleMove -> singleMove.destination),
				doubleMove -> doubleMove.destination2));
		int score = 0;
		if (move.commencedBy().equals(Piece.MrX.MRX)) {
			score = scoreMrX(board, destination) + scoreAdjacentNodes(board, destination);
			if (uselessMove(destination, move)) {
				score = 1;
			}
			if (losingMove(board, destination)) {
				score = 0;
			}
		} else {
			score = scoreDetective(board, destination) + scoreAdjacentNodes(board, destination);
			if (winningMove(board, destination)) {
				score = 100000;
			}
		}
		return score;
	}

	public int scoreMrX(Board board, int destination) {
		int score = scoreDistanceDetectives(board, destination);
		return score;
	}

	public int scoreDetective(Board board, int destination) {
		int score = scoreDistanceDetectives(board, getMrXLocation(board));
		return score;
	}

	public class Minimax {
		Tree tree;

		private class Node {
			private Move move;
			private int score;
			private boolean isMaxPlayer;
			private List<Node> children;
			private int depth;

			private Node(Move move, int score, int depth) {
				this.move = move;
				this.score = score;
				this.depth = depth;
				this.children = List.of();
			}

			public Node() {
			}

			public Move getMove() {
				return this.move;
			}

			public int getScore() {
				return this.score;
			}

			public void setScore(int score) {
				this.score = score;
			}

			public void setMaxPlayer() {
				switch (depth) {
					case 0:
					case 1:
						isMaxPlayer = false;
						break;
					case 2:
						isMaxPlayer = true;
						break;
				}
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

			private Tree() {
			}

			;

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
			Node root = new Node(null, score, depth);
			tree.setRoot(root);
			constructTree(root, depth, (Board.GameState) board);
		}

		private void constructTree(Node parentNode, int depth, Board.GameState gameState) {
			if (depth == 0) {
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(move, score(move, gameState), depth);
					newNode.setMaxPlayer();
					parentNode.addChildren(newNode);
				}
			} else {
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(move, score(move, gameState), depth);
					newNode.setMaxPlayer();
					if (move.commencedBy().equals(Piece.MrX.MRX)) {
						System.out.println(move);
						System.out.println(newNode.getScore() + "\n");
					}
					parentNode.addChildren(newNode);
					Board.GameState newGameState = gameState.advance(move);
					constructTree(newNode, depth - 1, newGameState);
				}
			}
		}

		private Move findBestMove() {
			Node bestNode = null;
			for (Node child : tree.getRoot().getChildren()) {
				if (child.getScore() == tree.getRoot().getScore()) {
					bestNode = child;
				}
			}
			return bestNode.getMove();
		}

		private void evaluateTree(Node node, int depth) {
			if (depth > 1) {
				for (Node child : node.getChildren()) {
					evaluateTree(child, depth - 1);
				}
			}
			Node bestChild = findBestChild(node.isMaxPlayer, node.getChildren());
			node.setScore(bestChild.getScore());
		}

		private Node findBestChild(boolean isMaxPlayer, List<Node> children) {
			Comparator<Node> byScoreComparator = Comparator.comparing(Node::getScore);
			Node bestChild = children.stream().max(isMaxPlayer ? byScoreComparator : byScoreComparator.reversed())
					.orElseThrow(NoSuchElementException::new);
			return bestChild;
		}
	}
}




