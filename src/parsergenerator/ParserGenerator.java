package parsergenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParserGenerator {
    private String pathToGrammar;
    private ArrayList<Symbol> grammar; // Each grammar line is defined by it's non terminal, each non terminal contains it's productions
    private HashMap<Symbol, HashSet<String>> firstSets;
    private HashMap<Symbol, HashSet<String>> followSets;

    public ParserGenerator(String pathToGrammar) {
        this.pathToGrammar = pathToGrammar;
    }

    public Parser getParser() {
        readProductions();

        leftFactorGrammar();

        eliminateLeftRecursion();

        calculateFirstSets();

        calculateFollowSets();

        ParseTable parseTable = new ParseTable(firstSets, followSets);

        return new Parser(parseTable);
    }

    private void readProductions() {
        grammar = new ArrayList<>();
        // ......
    }

    private void leftFactorGrammar() {
        ArrayList<Symbol> leftFactoredGrammar = new ArrayList<>();
        for (Symbol nonTerminal : grammar) {
            leftFactoredGrammar.addAll(nonTerminal.leftFactorProductions());
        }

        grammar = leftFactoredGrammar;
    }

    private void eliminateLeftRecursion() {
    }

    private void calculateFirstSets() {
        firstSets = new HashMap<>();
    }

    private void calculateFollowSets() {
        followSets = new HashMap<>();
    }
}
