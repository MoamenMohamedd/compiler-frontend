package parsergenerator;

import org.javatuples.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ParseTable {
    private Pair<Symbol, Integer>[][] parseTable; // Non terminal + which production in it
    private HashMap<Symbol, Integer> nonTerminalsIndexes;
    private HashMap<Symbol, Integer> terminalsIndexes;
    private HashMap<Symbol, HashSet<String>> firstSets;
    private HashMap<Symbol, HashSet<String>> followSets;

    public ParseTable(HashMap<Symbol, HashSet<String>> firstSets, HashMap<Symbol, HashSet<String>> followSets) {
        this.firstSets = firstSets;
        this.followSets = followSets;
        Set<String> terminals = getTerminals();

        this.parseTable = new Pair[this.firstSets.size()][terminals.size() + 1];
        this.nonTerminalsIndexes = new HashMap<>();
        this.terminalsIndexes = new HashMap<>();
    }

    private Set<String> getTerminals() {
        Set<String> terminals = new HashSet<>();
        for (Set<String> set : firstSets.values()) {
            terminals.addAll(set);
        }

        for (Set<String> set : followSets.values()) {
            terminals.addAll(set);
        }

        return terminals;
    }

    public Pair<Symbol, Integer> get(Symbol nonTerminal, Symbol terminal) {
        return parseTable[nonTerminalsIndexes.get(nonTerminal)][terminalsIndexes.get(terminal)];
    }
}
