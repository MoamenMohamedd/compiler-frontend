package lexicalanalyzergenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NFA implements Cloneable {

    private static int counter = 1;

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    class State {
        private int label;
        private List<Edge> edges;
        private boolean isStart;
        private boolean isFinal;

        public State(int label, boolean isStart, boolean isFinal) {
            this.isStart = isStart;
            this.isFinal = isFinal;
            this.label = label;
            edges = new ArrayList<>();
        }

        public void addEdge(State to, char input) {
            edges.add(new Edge(this, to, input));
        }

        @Override
        public boolean equals(Object obj) {
            return this.label == ((State) obj).label;
        }
    }

    class Edge {
        private State from;
        private State to;
        char input;

        public Edge(State from, State to, char input) {
            this.from = from;
            this.to = to;
            this.input = input;
        }


    }

    private State startState;
    private State finalState;

    /**
     * When we create an NFA for the first time
     * it consists of one start state and one final state
     * and one edge in between with weight -> character input
     *
     * @param input: char
     */
    public NFA(char input) {
        startState = new State(counter++, true, false);
        finalState = new State(counter++, false, true);

        startState.addEdge(finalState, input);
    }

    /**
     * This constructor is used when we create a resultant of
     * doing an operation on two NFAs
     *
     * @param startState State
     * @param finalState State
     */
    public NFA(State startState, State finalState) {
        this.startState = startState;
        this.finalState = finalState;
    }


    /**
     * Concatenates this NFA with another NFA
     *
     * @param other: NFA
     * @return NFA
     */
    public NFA concat(NFA other) {
        other.startState.isStart = false;
        this.finalState.isFinal = false;

        for (Edge edge : other.startState.edges) {
            edge.from = this.finalState;
        }

        this.finalState.edges = other.startState.edges;

        NFA result = new NFA(this.startState, other.finalState);

        return result;
    }

    /**
     * Applies or between this NFA and another NFA
     *
     * @param other: NFA
     * @return NFA
     */
    public NFA or(NFA other) {
        State newStart = new State(counter++, true, false);
        State newFinal = new State(counter++, false, true);

        this.startState.isStart = false;
        other.startState.isStart = false;

        this.finalState.isFinal = false;
        other.finalState.isFinal = false;

        newStart.addEdge(this.startState, '~'); // ~ is epsilon
        newStart.addEdge(other.startState, '~');

        this.finalState.addEdge(newFinal, '~');
        other.finalState.addEdge(newFinal, '~');

        NFA result = new NFA(newStart, newFinal);

        return result;
    }

    /**
     * Applies kleene closure to this NFA
     *
     * @return NFA
     */
    public NFA kleeneClosure() {
        State newStart = new State(counter++, true, false);
        State newFinal = new State(counter++, false, true);

        this.startState.isStart = false;
        this.finalState.isFinal = false;

        newStart.addEdge(this.startState, '~');
        this.finalState.addEdge(newFinal, '~');
        this.finalState.addEdge(this.startState, '~');
        newStart.addEdge(newFinal, '~');

        NFA result = new NFA(newStart, newFinal);

        return result;
    }

    /**
     * Applies positive closure to this NFA
     *
     * @return NFA
     */
    public NFA positiveClosure() {
        return this.concat(this.kleeneClosure());
    }

    public void visualize() {
        Set<State> visited = new HashSet<>();
        visualize(this.startState, visited);
    }

    private void visualize(State state, Set<State> visited) {
        if (visited.contains(state))
            return;

        if (state.edges.size() == 0)
            System.out.println("(" + state.label + ")");

        for (Edge edge : state.edges) {


            System.out.print("(" + edge.from.label + ")" + "---" + (edge.input == '~' ? "ep":edge.input) + "-->");
            visited.add(edge.from);
            visualize(edge.to, visited);
        }
    }
}
