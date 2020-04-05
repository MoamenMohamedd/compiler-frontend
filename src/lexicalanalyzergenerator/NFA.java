package lexicalanalyzergenerator;

import java.util.*;

public class NFA extends TransitionTable {

    private static int counter = 1;

    private State startState;
    private ArrayList<State> finalStates;
    private ArrayList<State> states;

    /**
     * When we create an NFA for the first time
     * it consists of one start state and one final state
     * and one edge in between with weight -> character input
     *
     * @param input: char
     */
    public NFA(char input) {
        this.startState = new State(counter++, true, false);
        State finalState = new State(counter++, false, true);

        startState.addEdge(finalState, input);

        finalStates = new ArrayList<>();
        finalStates.add(finalState);
    }

    /**
     * This constructor is used when we create a resultant of
     * doing an operation on two NFAs
     *
     * @param startState  State
     * @param finalStates ArrayList<State>
     */
    public NFA(State startState, ArrayList<State> finalStates) {
        this.startState = startState;
        this.finalStates = new ArrayList<>(finalStates);
    }

    /**
     * Concatenates this NFA with another NFA
     *
     * @param other: NFA
     * @return NFA
     */
    public NFA concat(NFA other) {
        other.startState.setStart(false);
        this.finalStates.get(0).setFinal(false);

        for (Edge edge : other.startState.getEdges()) {
            edge.setFrom(this.finalStates.get(0));
        }

        this.finalStates.get(0).setEdges(other.startState.getEdges());
        other.startState = null;

        NFA result = new NFA(this.startState, other.finalStates);

        return result;
    }


    /**
     * Applies positive closure to this NFA
     *
     * @return NFA
     */
    public NFA positiveClosure() {
        State newStart = new State(counter++, true, false);
        State newFinal = new State(counter++, false, true);

        this.startState.setStart(false);
        this.finalStates.get(0).setFinal(false);

        newStart.addEdge(this.startState, '~');
        this.finalStates.get(0).addEdge(newFinal, '~');
        this.finalStates.get(0).addEdge(this.startState, '~');

        ArrayList<State> newFinalStates = new ArrayList<>();
        newFinalStates.add(newFinal);

        NFA result = new NFA(newStart, newFinalStates);

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

            this.startState.setStart(false);
            other.startState.setStart(false);


            this.finalStates.get(0).setFinal(false);
            other.finalStates.get(0).setFinal(false);

            newStart.addEdge(this.startState, '~'); // ~ is epsilon
            newStart.addEdge(other.startState, '~');
            this.finalStates.get(0).addEdge(newFinal, '~');
            other.finalStates.get(0).addEdge(newFinal, '~');

            ArrayList<State> newFinalStates = new ArrayList<>();
            newFinalStates.add(newFinal);

            NFA result = new NFA(newStart, newFinalStates);

            return result;

        } else {

            this.startState.addEdge(other.startState, '~');
            other.finalStates.get(0).addEdge(this.finalStates.get(0), '~');
            other.startState.setStart(false);
            other.finalStates.get(0).setFinal(false);
            return this;
        }
    }

    /**
     * Applies kleene closure to this NFA
     *
     * @return NFA
     */
    public NFA kleeneClosure() {
        State newStart = new State(counter++, true, false);
        State newFinal = new State(counter++, false, true);

        this.startState.setStart(false);
        this.finalStates.get(0).setFinal(false);

        newStart.addEdge(this.startState, '~');
        this.finalStates.get(0).addEdge(newFinal, '~');
        this.finalStates.get(0).addEdge(this.startState, '~');
        newStart.addEdge(newFinal, '~');

        ArrayList<State> newFinalStates = new ArrayList<>();
        newFinalStates.add(newFinal);

        NFA result = new NFA(newStart, newFinalStates);

        return result;
    }

    /**
     * Combines all NFAs
     *
     * @param tokensNFA: Map of key value pairs. The key denotes the token that this NFA matches
     *                   while the value denotes the NFA
     * @return
     */
    public static NFA combineNFAs(ArrayList<NFA> tokensNFA) {

        State startState = new State(counter++, false, false);
        ArrayList<State> finalStates = new ArrayList<>();

        for (int i = 0; i < tokensNFA.size(); i++) {
            /* append new start to nfa start */
            startState.addEdge(tokensNFA.get(i).getStartState(), '~');
            tokensNFA.get(i).getStartState().setStart(false);

            /* store all final states */
            finalStates.addAll(tokensNFA.get(i).getFinalStates());
        }


        return new NFA(startState, finalStates);
    }

    public State getStartState() {
        return startState;
    }

    public ArrayList<State> getFinalStates() {
        return finalStates;
    }

    public void setMatches(String key) {
        this.finalStates.get(0).setToken(key);
    }


    public void print() {
        System.out.println("----------------------------------------");
        super.print(startState, new HashSet<>());
        System.out.println("----------------------------------------");
    }


}
