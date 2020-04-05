package lexicalanalyzergenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Stack;

public class NFA implements Cloneable {

    public static int counter = 0;

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    class State {
        private int label;
        private List<Edge> edges;
        private boolean isStart;
        private boolean isFinal;
        private String token;
        private int count;

        public State(int label, boolean isStart, boolean isFinal) {
            this.isStart = isStart;
            this.isFinal = isFinal;
            this.label = label;
            this.token = "";
            edges = new ArrayList<>();
            this.count = 0;
        }

        public void addEdge(State to, char input) {
            this.count++;
            edges.add(new Edge(this, to, input));
        }

        public int getCount() {
            return this.count;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public boolean equals(Object obj) {
            return this.label == ((State) obj).label;
        }
    }

    public State getNewState(boolean isStart, boolean isFinal) {
        State newState = new State(counter++, isStart, isFinal);
        return newState;
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

    public void setIsStart(boolean isStart) {
        this.startState.isStart = isStart;
    }

    public void setIsFinal(boolean isFinal) {
        this.finalState.isFinal = isFinal;
    }


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

    public State getStartState() {
        return startState;
    }

    public State getFinalState() {
        return finalState;
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

    public NFA or(NFA other, boolean lastIsOr) {

        if (!lastIsOr) {
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

        } else {

            this.startState.addEdge(other.startState, '~');
            other.finalState.addEdge(this.finalState, '~');
            other.startState.isStart = false;
            other.finalState.isFinal = false;
            return this;
        }
    }

    /**
     * Combines all NFAs
     *
     * @param tokensNFA: Map of key value pairs. The key denotes the token that this NFA matches
     *                   while the value denotes the NFA
     * @return
     */
    public static State combineNFAs(ArrayList<NFA> tokensNFA) {

        State startState = tokensNFA.get(0).getNewState(true, false);

        for (int i = 0; i < tokensNFA.size(); i++) {

            /* append new start to nfa start */
            startState.addEdge(tokensNFA.get(i).getStartState(), '~');
            tokensNFA.get(i).setIsStart(false);
        }

        return startState;
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
        System.out.println("START === " + result.startState.label);
        return result;
    }

    public NFA positiveClosure() throws CloneNotSupportedException {
        return this.concat((NFA) this.kleeneClosure().clone());
    }

    /**
     * Applies positive closure to this NFA
     *
     * @return NFA
     */


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


            System.out.print("(" + edge.from.label + ")" + "---" + (edge.input == '~' ? "ep" : edge.input) + "-->");
            visited.add(edge.from);
            visualize(edge.to, visited);
        }
    }

    public void visualizegraph() {
        Set<State> visited = new HashSet<>();
        visualizegraph(this.startState, visited);
    }


    private void visualizegraph(State state, Set<State> visited) {

        if (visited.contains(state))
            return;

//        if (state.edges.size() == 0)
//            System.out.println(state.label);

        for (Edge edge : state.edges) {
            System.out.println(edge.from.label + " " +  edge.to.label + " " + (edge.input == '~' ? "ep" : edge.input) + " ");
            visited.add(edge.from);
            visualizegraph(edge.to, visited);
        }

    }

}
