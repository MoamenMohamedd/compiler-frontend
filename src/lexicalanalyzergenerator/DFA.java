package lexicalanalyzergenerator;

import java.util.*;


public class DFA extends TransitionTable {

    public DFA(NFA nfa, Set<Character> inputSymbols) {
        this.nfa = nfa;
        this.subsets = new HashMap<>();
        this.inputSymbols = inputSymbols;
        this.groups = new HashMap<>();

        convert();

        print();
    }


    private NFA nfa;
    private Set<Character> inputSymbols;
    private HashMap<Set<State>, State> subsets;
    private HashMap<Integer, Set<State>> groups;
    private int counter = 0;
    private State startState;
    private State current;

    private void convert() {
        groups.put(0, new HashSet<>());
        groups.put(1, new HashSet<>());
        Queue<Set<State>> queue = new ArrayDeque<>();
        Set<Set<State>> marked = new HashSet<>();

        Set<State> initialEclosure = eClosure(nfa.getStartState());
        startState = new State(counter++, true, false);
        subsets.put(initialEclosure, startState);
        queue.add(initialEclosure);

        while (!queue.isEmpty()) {
            Set<State> set = queue.remove();
            marked.add(set);
            for (Character input : inputSymbols) {
                Set<State> u = eClosure(move(set, input));

                if (u.isEmpty()) continue;

                if (!subsets.containsKey(u)) {
                    storeNewDfaState(u);
//                    State state = new State(counter++, false, false);
//                    subsets.put(u, state);
                    queue.add(u);
                }
                subsets.get(set).addEdge(subsets.get(u), input);
            }
        }
    }

    private void storeNewDfaState(Set<State> states) {
        ArrayList<State> finalStates = new ArrayList<>();

        for (State state : states) {

            if (state.isFinal())
                finalStates.add(state);
        }

        State state = null;
        if (finalStates.size() == 1) {
            state = new State(counter++, false, true);
            state.setToken(finalStates.get(0).getToken(), -1);
            groups.get(0).add(state);
        } else if (finalStates.size() > 1) {
            finalStates.sort((s1, s2) -> s1.getPriority() - s2.getPriority());
            state = new State(counter++, false, true);
            state.setToken(finalStates.get(0).getToken(), -1);
            groups.get(0).add(state);
        } else {
            state = new State(counter++, false, false);
            groups.get(1).add(state);
        }

        subsets.put(states, state);

    }


    private Set<State> eClosure(State state) {
        Set<State> set = new HashSet<>();
        eClosure(state, set);
        return set;
    }

    private void eClosure(State state, Set<State> set) {
        for (Edge edge : state.getEdges()) {
            if (edge.getInput() == '~') {
                set.add(edge.getTo());
                eClosure(edge.getTo(), set);
            }
        }
    }

    private Set<State> eClosure(Set<State> states) {
        Set<State> set = new HashSet<>();
        eClosure(states, set);
        return set;
    }

    private void eClosure(Set<State> states, Set<State> set) {
        Stack<State> stack = new Stack<>();
        for (State state : states) {
            stack.push(state);
            set.add(state);
        }

        while (!stack.isEmpty()) {
            State t = stack.pop();
            Set<State> t_eClosure = eClosure(t);
            for (State u : t_eClosure) {
                if (!set.contains(u)) {
                    set.add(u);
                    stack.push(u);
                }
            }
        }
    }

    private Set<State> move(Set<State> states, Character input) {
        Set<State> set = new HashSet<>();

        for (State state : states) {
            for (Edge edge : state.getEdges()) {
                if (edge.getInput() == input)
                    set.add(edge.getTo());
            }
        }

        return set;
    }

    public void print() {
        System.out.println("----------------------------------------");
        super.print(startState, new HashSet<>());
        System.out.println("----------------------------------------");
    }

    private void minimize() {
        int groupId = 0;
        for (Set<State> group : groups.values()) {

        }


    }

    public String advance(char input) {
        for (Edge edge : current.getEdges()) {
            if (edge.getInput() == input) {
                current = edge.getTo();
                return current.getToken();
            }
        }

        return null;
    }

    public void reset() {
        current = startState;
    }

}
