package lexicalanalyzergenerator;

import java.util.ArrayList;
import java.util.List;

public class NFA {
    private static int counter = 1;

    class State {
        private int label;
        private List<Edge> edges;
        private boolean isStart;
        private boolean isFinal;

        public State(int label, boolean isStart, boolean isFinal) {
            this.label = label;
            edges = new ArrayList<>();
        }

        public void addEdge(State to, char input) {
            edges.add(new Edge(this, to, input));
        }

        @Override
        public boolean equals(Object obj) {
            return this.label == ((State)obj).label;
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
    private ArrayList<State> internalStates; // States that are in between

    /**
     * When we create an NFA for the first time
     * it consists of one start state and one final state
     * and one edge in between with weight -> character input
     *
     * @param input: char
     */
    public NFA(char input){
        startState = new State(counter++, true,false);
        finalState = new State(counter++, false,true);

        startState.addEdge(finalState,input);
    }

    /**
     * This constructor is used when we create a resultant of
     * doing an operation on two NFAs
     *
     * @param startState State
     * @param finalState State
     * @param internalStates ArrayList<State>
     */
    public NFA(State startState, State finalState, ArrayList<State> internalStates){
        this.startState = startState;
        this.finalState = finalState;
        this.internalStates = internalStates;
    }


    public State getStartState() {
        return startState;
    }

    public State getFinalState() {
        return finalState;
    }

    public ArrayList<State> getInternalStates() {
        return internalStates;
    }

    /**
     * Concatenates this NFA with another NFA
     * @param other: NFA
     * @return NFA
     */
    public NFA concat(NFA other) {
        other.startState.isStart = false;
        this.finalState.isFinal = false;

        for (Edge edge : other.startState.edges) {
            edge.from = this.finalState;
        }

        ArrayList<State> combinedInternalStates = new ArrayList<>();
        combinedInternalStates.addAll(this.internalStates);
        combinedInternalStates.addAll(other.internalStates);

        NFA result = new NFA(this.startState,other.finalState,combinedInternalStates);

        return result;
    }

    /**
     * Applies or between this NFA and another NFA
     * @param other: NFA
     * @return NFA
     */
    private NFA or(NFA other) {
        State newStart = new State(counter++,true,false);
        State newFinal = new State(counter++,false,true);

        this.startState.isStart = false;
        other.startState.isStart = false;

        this.finalState.isFinal = false;
        other.finalState.isFinal = false;

        newStart.addEdge(this.startState,'~'); // ~ is epsilon
        newStart.addEdge(other.startState,'~');

        this.finalState.addEdge(newFinal,'~');
        other.finalState.addEdge(newFinal,'~');

        ArrayList<State> combinedInternalStates = new ArrayList<>();
        combinedInternalStates.addAll(this.internalStates);
        combinedInternalStates.addAll(other.internalStates);

        NFA result = new NFA(newStart,newFinal,combinedInternalStates);

        return result;
    }

    /**
     * Applies kleene closure to this NFA
     * @return NFA
     */
    private NFA kleeneClosure() {
        State newStart = new State(counter++,true,false);
        State newFinal = new State(counter++,false,true);

        this.startState.isStart = false;
        this.finalState.isFinal = false;

        newStart.addEdge(this.startState,'~');
        this.finalState.addEdge(newFinal,'~');
        newStart.addEdge(newFinal,'~');

        ArrayList<State> combinedInternalStates = new ArrayList<>();
        combinedInternalStates.addAll(this.internalStates);

        NFA result = new NFA(newStart,newFinal,combinedInternalStates);

        return result;
    }

    /**
     * Applies positive closure to this NFA
     * @return NFA
     */
    private NFA positiveClosure() {
        return null;
    }
}
