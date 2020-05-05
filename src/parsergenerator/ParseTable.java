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
        this.firstSets = new HashMap<>();
        for (Map.Entry<Symbol, HashSet<String>> entry : firstSets.entrySet()) {
            if (entry.getKey().isNonTerminal())
                this.firstSets.put(entry.getKey(), entry.getValue());
        }
        this.followSets = followSets;

        System.out.println();

        setupTerminals();

        setupNonTerminals();

        setupProductions();

        print();
    }

    private void setupTerminals() {
        terminalsIndexes = new LinkedHashMap<>();

        Set<String> terminals = new LinkedHashSet<>();
        for (Set<String> set : firstSets.values()) {
            terminals.addAll(set);
        }

        for (Set<String> set : followSets.values()) {
            terminals.addAll(set);
        }

        terminals.add("$");
        terminals.remove("\\L");

        int count = 0;
        for (String terminal : terminals) {
            terminalsIndexes.put(terminal, count++);
        }

        System.out.println("Parse table terminals: " + terminalsIndexes.keySet());

    }

    private void setupNonTerminals() {
        nonTerminalsIndexes = new LinkedHashMap<>();

        int count = 0;
        for (Symbol nonTerminal : firstSets.keySet()) {
            nonTerminalsIndexes.put(nonTerminal.getLabel(), count++);
        }

        System.out.println("Parse table non terminals: " + nonTerminalsIndexes.keySet());
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
                    if (firstSymbol.getLabel().equals("\\L")) {
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
                if (parseTable[nonTerminalIndex][terminalIndex] == null)
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

    public void print() {
        for (String nonTerminal : nonTerminalsIndexes.keySet()) {
            for (String terminal : terminalsIndexes.keySet()) {
                Pair<Symbol, Integer> entry = parseTable[nonTerminalsIndexes.get(nonTerminal)][terminalsIndexes.get(terminal)];
                System.out.println("parseTable[" + nonTerminal + "]" + "[" + terminal + "]" + " = " + printProduction(entry));
            }
        }
    }

    public String printProduction(Pair<Symbol, Integer> production){
        if (production == null)
            return "null";

        if (production.getValue0() == null && production.getValue1() == null)
            return "sync";

        return production.getValue0().toString(production.getValue1());

    }

    public String getStartSymbol() {
        return null;
    }
}
