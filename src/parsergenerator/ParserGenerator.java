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
    }

    private void calculateFollowSets() {
        followSets = new HashMap<>();
    }



}
