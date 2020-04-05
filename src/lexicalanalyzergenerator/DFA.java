package lexicalanalyzergenerator;

import java.util.*;


public class DFA extends TransitionTable {

    public DFA(NFA nfa) {


        State state0 = new State(0, true, false);
        State state1 = new State(1, false, false);
        State state2 = new State(2, false, false);
        State state3 = new State(3, false, false);
        State state4 = new State(4, false, false);
        State state5 = new State(5, false, false);
        State state6 = new State(6, false, false);
        State state7 = new State(7, false, false);
        State state8 = new State(8, false, false);
        State state9 = new State(9, false, false);
        State state10 = new State(10, false, true);


        state0.addEdge(state1, '~');
        state0.addEdge(state7, '~');
        state1.addEdge(state2, '~');
        state1.addEdge(state4, '~');
        state2.addEdge(state3, 'a');
        state4.addEdge(state5, 'b');
        state3.addEdge(state6, '~');
        state5.addEdge(state6, '~');
        state6.addEdge(state1, '~');
        state6.addEdge(state7, '~');
        state7.addEdge(state8, 'a');
        state8.addEdge(state9, 'b');
        state9.addEdge(state10, 'b');

        ArrayList<State> fs = new ArrayList<>();
        fs.add(state10);
        NFA nf = new NFA(state0, fs);

        this.nfa = nf;


        this.subsets = new HashMap<>();

        this.inputSymbols = new ArrayList<>();
        this.inputSymbols.add('a');
        this.inputSymbols.add('b');

        convert();

        print();


    }


    private NFA nfa;
    private ArrayList<Character> inputSymbols;
    private HashMap<Set<State>, State> subsets;
    private int counter = 0;
    private State startState;
    private State current;

    private void convert() {
        Queue<Set<State>> queue = new ArrayDeque<>();
        Set<Set<State>> marked = new HashSet<>();

        Set<State> initialEclosure = eClosure(nfa.getStartState());
        storeNewDfaState(initialEclosure);
        queue.add(initialEclosure);

        while (!queue.isEmpty()) {
            Set<State> set = queue.remove();
            marked.add(set);
            for (Character input : inputSymbols) {
                Set<State> u = eClosure(move(set, input));

                if (!subsets.containsKey(u)) {
                    storeNewDfaState(u);
                    queue.add(u);
                }

                subsets.get(set).addEdge(subsets.get(u), input);
            }
        }
    }

    private void storeNewDfaState(Set<State> states) {
        ArrayList<State> startStates = new ArrayList<>();
        ArrayList<State> finalStates = new ArrayList<>();

        for (State state : states) {
            if (state.isStart())
                startStates.add(state);
            if (state.isFinal())
                finalStates.add(state);
        }

        State state = null;
        if (startStates.size() != 0) {
            state = new State(counter++, true, false);
        } else if (finalStates.size() == 1) {
            state = new State(counter++, false, true);
            state.setToken(finalStates.get(0).getToken());
        } else if (finalStates.size() > 1) {
            // Retrieve by priority
        } else {
            state = new State(counter++, false, false);
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
