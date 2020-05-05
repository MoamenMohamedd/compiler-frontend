package parsergenerator;

import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ParseTable {
    private Pair<Symbol, Integer>[][] parseTable; // Non terminal + which production in it
    private HashMap<String, Integer> nonTerminalsIndexes;
    private HashMap<String, Integer> terminalsIndexes;
    private HashMap<Symbol, HashSet<String>> firstSets;
    private HashMap<Symbol, HashSet<String>> followSets;

    public ParseTable(HashMap<Symbol, HashSet<String>> firstSets, HashMap<Symbol, HashSet<String>> followSets) {
        this.firstSets = firstSets;
        this.followSets = followSets;

        setupTerminals();

        setupNonTerminals();

        setupProductions();
    }

    private void setupTerminals() {
        terminalsIndexes = new HashMap<>();

        Set<String> terminals = new HashSet<>();
        for (Set<String> set : firstSets.values()) {
            terminals.addAll(set);
        }

        for (Set<String> set : followSets.values()) {
            terminals.addAll(set);
        }

        terminals.add("$");

        int count = 0;
        for (String terminal : terminals) {
            terminalsIndexes.put(terminal, count++);
        }

    }

    private void setupNonTerminals() {
        nonTerminalsIndexes = new HashMap<>();

        int count = 0;
        for (Symbol nonTerminal : firstSets.keySet()) {
            nonTerminalsIndexes.put(nonTerminal.getLabel(), count++);
        }
    }

    private void setupProductions() {
        parseTable = new Pair[nonTerminalsIndexes.size()][terminalsIndexes.size()];

        for (Symbol nonTerminal : firstSets.keySet()) {
            ArrayList<ArrayList<Symbol>> productions = nonTerminal.getProductions();
            int nonTerminalIndex = nonTerminalsIndexes.get(nonTerminal.getLabel());

            for (int i = 0; i < productions.size(); i++) {
                ArrayList<Symbol> production = productions.get(i);
                Symbol firstSymbol = production.get(0);

                if (firstSymbol.isTerminal()) {
                    if (firstSymbol.getLabel().equals("epsilon")) {
                        HashSet<String> followSet = followSets.get(nonTerminal);
                        for (String terminal : followSet) {
                            int terminalIndex = terminalsIndexes.get(terminal);
                            parseTable[nonTerminalIndex][terminalIndex] = new Pair<>(nonTerminal, i);
                        }
                    } else {
                        int terminalIndex = terminalsIndexes.get(firstSymbol.getLabel());
                        parseTable[nonTerminalIndex][terminalIndex] = new Pair<>(nonTerminal, i);
                    }
                } else {
                    HashSet<String> firstSet = firstSets.get(firstSymbol);
                    for (String terminal : firstSet) {
                        int terminalIndex = terminalsIndexes.get(terminal);
                        parseTable[nonTerminalIndex][terminalIndex] = new Pair<>(nonTerminal, i);
                    }
                }
            }
        }


        // Sync entries
        for (Symbol nonTerminal : firstSets.keySet()) {
            HashSet<String> followSet = followSets.get(nonTerminal);
            int nonTerminalIndex = nonTerminalsIndexes.get(nonTerminal.getLabel());

            for (String terminal : followSet) {
                int terminalIndex = terminalsIndexes.get(terminal);
                if (parseTable[nonTerminalIndex][terminalIndex] != null)
                    parseTable[nonTerminalIndex][terminalIndex] = new Pair<>(null, null); // sync
            }
        }

    }

    public List<String> get(String nonTerminal, String terminal) {
        Pair<Symbol, Integer> entry = parseTable[nonTerminalsIndexes.get(nonTerminal)][terminalsIndexes.get(terminal)];

        if (entry == null)
            return null;

        if (entry.getValue0() == null && entry.getValue1() == null)
            return new ArrayList<>();


        ArrayList<Symbol> production = entry.getValue0().getProduction(entry.getValue1());
        return production.stream().map(Symbol::getLabel).collect(Collectors.toList());
    }

    public String getStartSymbol() {
        return null;
    }
}
