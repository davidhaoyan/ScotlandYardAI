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
		new Minimax().minimax(board).showChildren();
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
		assert (move.commencedBy() == Piece.MrX.MRX);
		int destination = move.accept(new Move.FunctionalVisitor<>((singleMove -> singleMove.destination),
				doubleMove -> doubleMove.destination2));
		int score = scoreDistanceDetectives(board, destination);
		if (losingMove(board, destination)) return 0;
		return score;
	}

	/*	private static int evaluate() {
			int childEval = scoreDistanceDetectives(Board board, int destination);
			int ParentEVal = ;
			int evaluation = ChildEval - ParentEval;

		}

		private int search(int depth, Board board, int alpha, int beta){
			ImmutableSet<Move> moves = board.getAvailableMoves();
			if (depth == 0){
				return evaluate();
			}
			for (Move move: moves){
				//use simulated gamestate (overriden advance move)
				int evaluation = -Search(depth -1);
				bestEvaluation = Max (evaluation, bestEvaluation);
			//search the new game state depth -1;
				// make gamestate == original
			}
		}
	*/

	public class Minimax {
		Tree tree;

		private class Node {
			private Move move;
			private int score;
			private Board.GameState gameState;
			private boolean isMaxPlayer;
			private List<Node> children;

			private Node(Move move, int score, Board.GameState gameState, boolean isMaxPlayer) {
				this.move = move;
				this.score = score;
				this.gameState = gameState;
				this.isMaxPlayer = isMaxPlayer;
				this.children = List.of();
			}

			public Move getMove() {
				return this.move;
			}

			public int getScore() {
				return this.score;
			}

			public Board.GameState gameState() {
				return this.gameState;
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
			Node root = new Node(null, 0, (Board.GameState) board, true);
			tree.setRoot(root);
			constructTree(root, depth, (Board.GameState) board);
		}

		private void constructTree(Node parentNode, int depth, Board.GameState gameState) {
			System.out.println("moves:" + gameState.getAvailableMoves().size());
			System.out.println(gameState.getAvailableMoves());
			System.out.println(depth);

			if (depth == 0) {
				int i = 0;
				for (Move move : gameState.getAvailableMoves()) {
					System.out.println("zeros:" + i++);
					Node newNode = new Node(move, score(move, gameState), gameState, true);
					parentNode.addChildren(newNode);
				}
				System.out.println("end");
			}
			else {
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(move, score(move, gameState), gameState, true);
					parentNode.addChildren(newNode);
					Board.GameState newGameState = gameState.advance(move);
					constructTree(newNode, depth - 1, newGameState);
				}
			}
		}

		public Tree minimax(Board board) {
			constructTree(0, 6, board);
			return tree;
		}
	}
}

	/*class Node<T> {
		T data;
		Node<T> parent;
		List<Node<T>> branches;
		int score;

		public Node(T data) {
			this.data = data;
			this.branches = new ArrayList<>();
			this.score = score;
		}

		public List<Node<T>> getBranches(){
			return branches;
		}
		public int getScore(){
			return score;
		}
		public T getData(){
			return data;
		}
		public void setData(T data){
			this.data = data;
		}

		public void setParent(Node<T> parent){
			this.parent = parent;
		}
		public Node<T> getParent(){
			return parent;
		}

}

		public Node<T> addBranch(Node<T> branch){
			branch.setParent(this);
			this.branches.add(branch);
			return branch;
		}

		public void addBranches(List<Node<T>> branches){
			branches.forEach(each -> each.setParent(this));
			this.branches.addAll(branches);
		}
	}

	public class MiniMax{
		public void NewTree(Board board, Node node){
			Node root = new Node(board);
			constructTree(board, root);
		}
		public void constructTree(Board board, Node node){
			ImmutableSet<Move> moves = board.getAvailableMoves();
			for(Move move:moves){
				advance(move);
				node.addBranch(move);


	public Graph
	public void dijkstra(Board board, Move move){
		for (node)

	}
}
*/




