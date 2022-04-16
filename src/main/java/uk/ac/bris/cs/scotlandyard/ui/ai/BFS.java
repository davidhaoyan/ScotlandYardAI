package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

// breadth first search algorithm
public class BFS {

    // returns a list of tuples which represents the visited status of a node and its steps away from the origin
    static private ArrayList<Pair<Boolean,Integer>> getVisited(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph) {
        ArrayList<Pair<Boolean,Integer>> visited = new ArrayList<>();
        for (int node : graph.nodes()) {
            visited.add(Pair.pair(false, 0));
        }
        return visited;
    }

    // returns shortest distance between start node and end node
    static public int BFS(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, int start, int end) {
        ArrayList<Pair<Boolean,Integer>> visited = getVisited(graph);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visited.set(start-1, Pair.pair(true, 0));

        while (queue.size() != 0) {
            int current = queue.poll();
            if (current == end) return visited.get(current-1).right();
            for (int node : graph.adjacentNodes(current)) {
                if (!visited.get(node-1).left()) {
                    queue.add(node);
                    visited.set(node-1, Pair.pair(true, visited.get(current-1).right() + 1));
                }
            }
        }
        return 0;
    }
}
