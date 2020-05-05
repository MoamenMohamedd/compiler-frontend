package parsergenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.System.exit;

public class ParserGenerator {
    private String pathToGrammar;
    private ArrayList<Symbol> grammar; // Each grammar line is defined by it's non terminal, each non terminal contains it's productions
    private HashMap<Symbol, HashSet<String>> firstSets;
    private HashMap<Symbol, HashSet<String>> followSets;
    private HashMap<String, Symbol> symbols = new HashMap<>();

    public ParserGenerator(String pathToGrammar) {
        this.pathToGrammar = pathToGrammar;
    }

    public Parser getParser() {

        File file = new File(pathToGrammar);

        readProductions(file);

        eliminateLeftRecursion();

        leftFactorGrammar();

        calculateFirstSets();

        calculateFollowSets();

        firstSets.forEach((key, value) -> System.out.println("Label: " + key.getLabel() + ", First Set: " + value.toString()));

        System.out.println();
        System.out.println();
        System.out.println();

        followSets.forEach((key, value) -> System.out.println("Label: " + key.getLabel() + ", Follow Set: " + value.toString()));

        ParseTable parseTable = new ParseTable(firstSets, followSets);

        return new Parser(parseTable);
    }

    private void readProductions(File file) {
        Symbol sym = null;
        grammar = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            int arrayIndex = -1;
            int charIndex = 0;
            int symbolIndex = 0;
            ArrayList<ArrayList<Symbol>> rightProd = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    line = line.substring(1, line.length());
                    arrayIndex += 1;
                    String[] production = line.split("::=");
                    rightProd = new ArrayList<>();
                    if (arrayIndex == 0 || !symbols.containsKey(production[0].trim())) {
                        sym = new Symbol(production[0].trim(), rightProd);
                    } else {
                        sym = symbols.get(production[0].trim());
                    }
                    rightProd = getRightProductions(production[1].trim());
                    sym.setProductions(rightProd);
                    symbols.put(production[0].trim(), sym);
                    grammar.add(sym);
                }
                // If line starts with and or
                else {
                    grammar.remove(sym);
                    rightProd.addAll(getRightProductions(line));
                    sym.setProductions(rightProd);
                    grammar.add(sym);
                }
            }
            fileReader.close();    //closes the stream and release the resources
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ArrayList<Symbol>> getRightProductions(String rightProduction) {
        ArrayList<ArrayList<Symbol>> right = new ArrayList<>();
        String[] splits = rightProduction.split("\\|"); // split on the or than means we have more than one row

        int terminal = 0;
        Symbol sym;
        String temp;
        int index;
        for (int i = 0; i < splits.length; i++) {
            if (splits[i].length() == 0) {
                continue;
            }
            ArrayList<Symbol> row = new ArrayList<>();
            String label = "";
            temp = splits[i].trim();
            String[] syms = temp.split(" ");

            for (int j = 0; j < syms.length; j++) {
                // Non terminal
                if (syms[j].trim().startsWith("‘")) {
                    index = syms[j].lastIndexOf("’");
                    if (index == -1) {
                        System.err.println("Wrong Grammar");
                        exit(0);
                    }
                    label = syms[j].trim().substring(1, index);
                    sym = new Symbol(label);
                    row.add(sym);
                } else if (syms[j].trim().equals("\\L")) {
                    label = "epsilon";
                    sym = new Symbol(label);
                    row.add(sym);
                } else {
                    ArrayList<ArrayList<Symbol>> rightProd = new ArrayList<>();
                    label = syms[j].trim();
                    if (!symbols.containsKey(label)) {
                        sym = new Symbol(label, rightProd);
                    } else {
                        sym = symbols.get(label);
                    }

                    symbols.put(label, sym);
                    row.add(sym);
                }
            }
            right.add(row);
        }

        return right;
    }

    private void leftFactorGrammar() {
        ArrayList<Symbol> leftFactoredGrammar = new ArrayList<>();
        for (Symbol nonTerminal : grammar) {
            leftFactoredGrammar.addAll(nonTerminal.leftFactorProductions());
        }

        grammar = leftFactoredGrammar;
    }

    private void eliminateLeftRecursion() {
        ArrayList<Symbol> leftRecursionGrammar = new ArrayList<>();
        int index;
        for (int i = 0; i < grammar.size(); i++) {

            Symbol sym1 = grammar.get(i);           //Ai
            ArrayList<ArrayList<Symbol>> sym1Production = sym1.getProductions();     //production of Ai

            for (int j = 0; j <= i - 1; j++) {
                Symbol sym2 = grammar.get(j);       //Aj
                index = 0;
                //Parse the array of production of Ai
                for (ArrayList<Symbol> production : sym1Production) {

                    //Check if the first element in the production is equals to Aj non terminal
                    //Check is this non terminal has been seen before
                    if (production.get(0).getLabel().equals(sym2.getLabel())) {
                        ArrayList<ArrayList<Symbol>> temp = replaceProductions(sym1Production, sym2.getProductions(), index);
                        sym1.setProductions(temp);
                    }
                    index++;
                }
            }

            leftRecursionGrammar.addAll(eliminateImmediateRecursion(sym1, sym1.getProductions()));
        }

        grammar = leftRecursionGrammar;

        for (Symbol s : leftRecursionGrammar)
            System.out.println(s);
    }

    private ArrayList<Symbol> eliminateImmediateRecursion(Symbol sym, ArrayList<ArrayList<Symbol>> symProduction) {
        ArrayList<Symbol> temp;
        ArrayList<Symbol> notModified;
        boolean flag = false;
        Symbol newSym = null;
        ArrayList<ArrayList<Symbol>> newProd = null;
        ArrayList<ArrayList<Symbol>> tempProd = new ArrayList<>();
        ArrayList<Symbol> eliminated = new ArrayList<>();
        eliminated.add(sym);

        for (ArrayList<Symbol> production : symProduction) {
            if (production.get(0).getLabel().equals(sym.getLabel())) {
                newProd = new ArrayList<>();
                newSym = new Symbol(sym.getLabel() + "'", newProd);
                flag = true;
                break;
            }
        }

        if (flag) {
            for (ArrayList<Symbol> production : symProduction) {
                if (production.get(0).getLabel().equals(sym.getLabel())) {
                    temp = new ArrayList<>();
                    temp.addAll(production);
                    temp.remove(0);
                    temp.add(newSym);
                    newProd.add(temp);
                } else {
                    notModified = new ArrayList<>();
                    notModified.addAll(production);
                    notModified.add(newSym);
                    tempProd.add(notModified);
                }

            }
            Symbol epsilon = new Symbol("epsilon");
            temp = new ArrayList<>();
            temp.add(epsilon);
            newProd.add(temp);
            sym.setProductions(tempProd);
            newSym.setProductions(newProd);
            eliminated.add(newSym);
        }
        return eliminated;
    }

    private ArrayList<ArrayList<Symbol>> replaceProductions(ArrayList<ArrayList<Symbol>> prod1, ArrayList<ArrayList<Symbol>> prod2, int index) {
        ArrayList<ArrayList<Symbol>> tempProd = new ArrayList<>();
        for (int i = 0; i < prod1.size(); i++) {
            if (i != index) {
                tempProd.add(prod1.get(i));
            } else {
                for (int j = 0; j < prod2.size(); j++) {
                    ArrayList<Symbol> temp = new ArrayList<>();
                    ArrayList<Symbol> production = prod1.get(i);
                    temp.addAll(prod2.get(j));

                    for (int k = 1; k < production.size(); k++) {
                        temp.add(production.get(k));
                    }
                    tempProd.add(temp);
                }
            }
        }

        return tempProd;
    }

    private void calculateFirstSets() {
        firstSets = new HashMap<>();

        HashSet<Symbol> seen = new HashSet<>();

        for (Symbol symbol : grammar) {
            if (!seen.contains(symbol)) {
                seen.add(symbol);
                firstSets.put(symbol, new HashSet<>());
                firstSet(symbol, seen);
            }
        }
    }

    private void firstSet(Symbol symbol, HashSet<Symbol> seen) {
        /*  If symbol is terminal then first(symbol) = {symbol} */
        if (symbol.isTerminal() || symbol.getLabel().equals("epsilon")) {
            /* Add the non-terminal or epsilon to the first sets */
            firstSets.put(symbol, new HashSet<String>() {{
                add(symbol.getLabel());
            }});

            return;
        }

        ArrayList<ArrayList<Symbol>> productionRule = symbol.getProductions();

        /* Get the first symbol in the rule */
        productionRule.forEach((rule) -> {

            Symbol child = null;
            for (int i = 0; i < rule.size(); i++) {
                child = rule.get(i);

                /* if the child first not computed before */
                if (!seen.contains(child)) {
                    seen.add(child);
                    firstSet(child, seen);
                }

                /* Add the child first to the parent first */
                for (String val : firstSets.get(child)) {
                    if (!firstSets.containsKey(symbol)) {
                        firstSets.put(symbol, new HashSet<String>() {{
                            add(val);
                        }});
                    } else {
                        firstSets.get(symbol).add(val);
                    }
                }

                /* If the child contains epsilon and not the right most symbol so remove it and continue through the next symbols */
                if (firstSets.get(symbol).contains("epsilon") && i != rule.size() - 1) {
                    firstSets.get(symbol).remove("epsilon");
                }

                /* Child doesn't contains epsilon or it is the last symbol */
                else {
                    break;
                }
            }
        });
    }

    private void secondRule() {
        Symbol curr = null;
        Symbol next = null;

        /* if A -> xBC is a production rule -> everything in FIRST(C) is FOLLOW(B) except epsilon */
        for (Symbol symbol : grammar)
            for (ArrayList<Symbol> child : symbol.getProductions())
                for (int i = 0; i < child.size() - 1; i++) {
                    /* Checking for the non terminals */
                    if (child.get(i).isNonTerminal()) {
                        /* current non terminal */
                        curr = child.get(i);
                        next = child.get(i + 1);

                        if (firstSets.containsKey(next)) {
                            followSets.get(curr).addAll(firstSets.get(next));

                            /* removing epsilon from the follow */
                            if (followSets.get(curr).contains("epsilon")) {
                                followSets.get(curr).remove("epsilon");
                            }
                        } else {
                            if (next.isTerminal()) {
                                followSets.get(curr).add(next.getLabel());
                            }
                        }

                    }
                }
    }

    private void thirdRule() {
        /* If ( A -> αB is a production rule ) or ( A -> αBβ is a production rule and epsilon is in FIRST(β) ) -> everything in FOLLOW(A) is in FOLLOW(B). and repeat until no changes */
        boolean changes = true, change, epsilon;
        int i;
        Symbol curr = null;

        /* repeating till no changes */
        while (changes) {
            changes = false;
            for (Symbol symbol : grammar)
                for (ArrayList<Symbol> child : symbol.getProductions()) {
                    /* Iterating from the right most symbol until leftmost or no epsilon in the first set of current symbol */
                    i = child.size() - 1;
                    epsilon = true;

                    while (epsilon && i >= 0) {
                        curr = child.get(i);

                        /* If current symbol is non terminal */
                        if (curr.isNonTerminal()) {
                            for (String f : followSets.get(symbol)) {
                                /* If terminal is not in follow set of current */
                                if (!followSets.get(curr).contains(f)) {
                                    changes = true;
                                    followSets.get(curr).add(f);
                                }
                            }

                            epsilon = firstSets.get(curr).contains("epsilon");
                        }
                        /* Current symbol is terminal */
                        else {
                            epsilon = false;
                        }

                        i--;
                    }
                }
        }
    }

    private void calculateFollowSets() {
        followSets = new HashMap<>();

        /* Initializing follow sets for the grammar */
        for (Symbol symbol : grammar) {
            followSets.put(symbol, new HashSet<>());
        }

        /* If S is the start symbol -> $ is in FOLLOW(S) */
        followSets.get(grammar.get(0)).add("$");

        /* if A -> xBC is a production rule -> everything in FIRST(C) is FOLLOW(B) except epsilon */
        secondRule();

        /* If ( A -> αB is a production rule ) or ( A -> αBβ is a production rule and epsilon is in FIRST(β) ) -> everything in FOLLOW(A) is in FOLLOW(B). and repeat until no changes */
        thirdRule();
    }

}
