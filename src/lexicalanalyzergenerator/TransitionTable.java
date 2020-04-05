package lexicalanalyzergenerator;

import java.util.Set;

public class TransitionTable {

    protected void print(State state, Set<State> visited) {

        if (visited.contains(state))
            return;

        visited.add(state);
        for (Edge edge : state.getEdges()) {
            System.out.println(edge);
            print(edge.getTo(), visited);
        }

    }
}
