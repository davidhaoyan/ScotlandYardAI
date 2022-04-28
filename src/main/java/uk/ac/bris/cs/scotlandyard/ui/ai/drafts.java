package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.ArrayList;
import java.util.List;

public class drafts {

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


    //DAVIDS
    /*
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


    */




    //MY ATTEMPT AT A DETECTIVE MOVE TREE

    /*
    public class Minimax{
		Tree tree;
	public class Node<T> {
		private T data;
		private Node<T> parent;
		private List<Node<T>> branches;
		private int score;
		private boolean isMaxPlayer;

		public Node(T data, Move move, Board board, boolean isMaxPlayer) {
			this.data = data;
			this.branches = new ArrayList<>();
			this.isMaxPlayer = isMaxPlayer;
			this.score = score;
		}

		public List<Node<T>> getBranches() {
			return branches;
		}

		public int getScore() {
			return score;
		}

		public T getData() {
			return data;
		}

		public void setData(T data) {
			this.data = data;
		}

		public void setParent(Node<T> parent) {
			this.parent = parent;
		}

		public Node<T> getParent() {
			return parent;
		}

		public Node<T> addBranch(Node<T> branch) {
			branch.setParent(this);
			this.branches.add(branch);
			return branch;
		}

		public void addBranches(List<Node<T>> branches) {
			branches.forEach(each -> each.setParent(this));
			this.branches.addAll(branches);
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

		public List showBranches() {
			return this.root.getBranches();
		}
	}
		private void NewTree(Board board,int depth) {
			tree = new Tree();
			Board.GameState gameState = (Board.GameState) board;
			Node root = new Node(gameState, null, board, true);
			tree.setRoot(root);
			constructTree(gameState, board, root, depth);
		}

		public void constructTree(Board.GameState gameState, Board board, Node parentNode, int depth) {{
			if (depth == 0){
				for (Move move : gameState.getAvailableMoves()) {
					Node newNode = new Node(gameState, move, board, false);
					parentNode.addBranch(newNode);
				}
				System.out.println("end");
			}
			else {
					if (parentNode.isMaxPlayer) {
						for (Move move : gameState.getAvailableMoves()) {
							Node newNode = new Node(gameState, move, board, false);
							newNode.score = score(move, board);
							parentNode.addBranch(newNode);
							Board.GameState newGameState = gameState.advance(move);
							System.out.println(newNode.score);
							System.out.println(move);
							constructTree(newGameState, board, newNode, depth - 1);
						}
					}


					if (parentNode.isMaxPlayer == false) {
							for (Piece piece : gameState.getPlayers()) {
								if (piece.isDetective()) {
									for (Move move : gameState.getAvailableMoves()) {
										Node newNode = new Node(gameState, move, board, false);
										newNode.score = score(move, board);
										parentNode.addBranch(newNode);
										Board.GameState newGameState = gameState.advance(move);
										System.out.println(newNode.score);
										constructTree(newGameState, board, newNode, depth - 1);
									}
								}
							}
						}
					}
				}
			}




		public Tree minimax(Board board, int depth) {
			NewTree(board,3);
			return tree;
		}
     */
}
