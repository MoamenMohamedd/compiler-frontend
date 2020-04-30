package lexicalanalyzergenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class DFA extends TransitionTable {

    public DFA(NFA nfa, Set<Character> inputSymbols) {
        this.nfa = nfa;
        this.subsets = new HashMap<>();
        this.inputSymbols = inputSymbols;
//        this.inputSymbols = new HashSet<>();
//        this.inputSymbols.add('0');
//        this.inputSymbols.add('1');

//        State state0 = new State(0,true,false);
//        State state1 = new State(1,false,true);
//        state1.setToken("id",3);
//        State state2 = new State(2,false,true);
//        state2.setToken("if",2);
//        State state3 = new State(3,false,false);
//        State state4 = new State(4,false,true);
//        state4.setToken("int",1);
//        State state5 = new State(5,false,false);
//
//        state0.addEdge(state3,'0');
//        state0.addEdge(state1,'1');
//        state1.addEdge(state2,'0');
//        state1.addEdge(state5,'1');
//        state2.addEdge(state2,'0');
//        state2.addEdge(state5,'1');
//        state3.addEdge(state0,'0');
//        state3.addEdge(state4,'1');
//        state4.addEdge(state2,'0');
//        state4.addEdge(state5,'1');
//        state5.addEdge(state5,'0');
//        state5.addEdge(state5,'1');
//
//        this.temp.add(state0);
//        this.temp.add(state1);
//        this.temp.add(state2);
//        this.temp.add(state3);
//        this.temp.add(state4);
//        this.temp.add(state5);
//
//
//
//        this.startState = state0;


        convert();
//        minimize();

        print();

    }


    private NFA nfa;
    private Set<Character> inputSymbols;
    private HashMap<Set<State>, State> subsets;
    private int counter = 0;
    private State startState;
    private State current;
//    public ArrayList<State> temp = new ArrayList<>();

    private void convert() {
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
            state.setToken(finalStates.get(0).getToken(), finalStates.get(0).getPriority());
        } else if (finalStates.size() > 1) {
            finalStates.sort((s1, s2) -> s1.getPriority() - s2.getPriority());
            state = new State(counter++, false, true);
            state.setToken(finalStates.get(0).getToken(), finalStates.get(0).getPriority());
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

    private void minimize() {
        ArrayList<Set<State>> partition = new ArrayList<>();
        partition.add(new LinkedHashSet<>());
        partition.add(new LinkedHashSet<>());

        for (State state : this.subsets.values()) {
            if (state.isStart() || !(state.isStart() || state.isFinal()))
                partition.get(1).add(state);
            if (state.isFinal())
                partition.get(0).add(state);
        }


        ArrayList<Set<State>> oldPartition = null;
        ArrayList<Set<State>> newPartition = null;
        do {
            oldPartition = newPartition != null ? newPartition : partition;
            newPartition = partition(oldPartition);
        } while (!newPartition.equals(oldPartition));


        Map<Integer, State> pickedStates = new HashMap<>();
        for (int i = 0; i < newPartition.size(); i++) {
            if (newPartition.get(i).size() == 1) {
                pickedStates.put(i, newPartition.get(i).iterator().next());
                continue;
            }

            ArrayList<State> startStates = new ArrayList<>();
            ArrayList<State> finalStates = new ArrayList<>();
            ArrayList<State> otherStates = new ArrayList<>();

            for (State state : newPartition.get(i)) {
                if (state.isStart())
                    startStates.add(state);
                else if (state.isFinal())
                    finalStates.add(state);
                else
                    otherStates.add(state);
            }


            if (!otherStates.isEmpty()) {
                pickedStates.put(i, otherStates.get(0));
                continue;
            }

            if (!finalStates.isEmpty()) {
                finalStates.sort((s1, s2) -> s1.getPriority() - s2.getPriority());
                pickedStates.put(i, finalStates.get(0));
                continue;
            }

            if (!startStates.isEmpty()) {
                pickedStates.put(i, startStates.get(0));
                continue;
            }


        }

        for (State state : pickedStates.values()) {
            for (Edge edge : state.getEdges()) {
                edge.setTo(getPickedState(edge.getTo(), newPartition, pickedStates));
            }
        }

        try {
            FileWriter writer = new FileWriter("transitionTable.csv");

            for (Character ch : inputSymbols) {
                writer.append(ch + ",");
            }
            writer.append("\n");

            for (State state : pickedStates.values()) {
                writer.append(state.getLabel() + " ");
                for (Character ch : inputSymbols) {
                    String stateLabel = advance(state, ch) == null ? "0" : advance(state, ch).getLabel() + "";
                    writer.append(stateLabel);
                    writer.append(",");
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println();
    }

    private State getPickedState(State state, ArrayList<Set<State>> partition, Map<Integer, State> pickedStates) {
        for (int i = 0; i < partition.size(); i++) {
            if (partition.get(i).contains(state))
                return pickedStates.get(i);
        }
        return null;
    }

    private ArrayList<Set<State>> partition(ArrayList<Set<State>> partition) {
        ArrayList<Set<State>> newPartition = new ArrayList<>();

        for (Set<State> set : partition) {
            if (set.size() == 1) {
                newPartition.add(set);
                continue;
            }

            Iterator<State> iterator1 = set.iterator();
            Iterator<State> iterator2 = set.iterator();
            iterator2.next();

            Set<State> newSet1 = new HashSet<>();
            Set<State> newSet2 = new HashSet<>(set);
            while (iterator1.hasNext()) {
                while (iterator2.hasNext()) {
                    State s1 = iterator1.next();
                    State s2 = iterator2.next();

                    if (areDistinguishable(s1, s2, partition)) {
                        newSet1.add(s2);
                        newSet2.remove(s2);
                    }
                }
                if (!newSet1.isEmpty())
                    newPartition.add(newSet1);

                newPartition.add(newSet2);
                break;
            }

        }

        return newPartition;
    }

    private boolean areDistinguishable(State s1, State s2, ArrayList<Set<State>> partition) {
        boolean distinguishable = false;

        for (Character ch : inputSymbols) {
            State s1Advanced = advance(s1, ch);
            State s2Advanced = advance(s2, ch);
            if (areInDifferentSets(s1Advanced, s2Advanced, partition))
                distinguishable = true;
        }

        return distinguishable;
    }

    private boolean areInDifferentSets(State s1, State s2, ArrayList<Set<State>> partition) {
        for (Set set : partition) {
            if (set.contains(s1) && !set.contains(s2))
                return true;
            if (set.contains(s2) && !set.contains(s1))
                return true;
        }

        return false;
    }

    private State advance(State state, char input) {
        for (Edge edge : state.getEdges()) {
            if (edge.getInput() == input) {
                return edge.getTo();
            }
        }
        return null;
    }

    public String advance(char input) {
        for (Edge edge : current.getEdges()) {
            if (edge.getInput() == input) {
                current = edge.getTo();

                return current.isFinal() ? current.getToken() : " ";
            }
        }

        return null;
    }

    public void reset() {
        current = startState;
    }

}
