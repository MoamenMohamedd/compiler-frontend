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

        File file = new File(this.pathToGrammar);

        readProductions(file);

        leftFactorGrammar();

        eliminateLeftRecursion();

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
                if(line.startsWith("#")){
                    line = line.substring(1, line.length());
                    arrayIndex+=1;
                    String[] production = line.split("::=");
                    rightProd = new ArrayList<>();
                    if(arrayIndex == 0 || !symbols.containsKey(production[0].trim())){
                        sym = new Symbol(production[0].trim(), rightProd);
                    }
                    else {
                        sym = symbols.get(production[0].trim());
                    }
                    rightProd = getRightProd(production[1].trim());
                    sym.setProductions(rightProd);
                    symbols.put(production[0].trim(),sym);
                    grammar.add(sym);
                }
                // If line starts with and or
                else{
                    grammar.remove(sym);
                    rightProd.addAll(getRightProd(line));
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

   private ArrayList<ArrayList<Symbol>> getRightProd(String rightProduction){
        ArrayList<ArrayList<Symbol>> right = new ArrayList<>();
        String [] splits = rightProduction.split("\\|"); // split on the or than means we have more than one row

        int terminal = 0;
        Symbol sym;
        String temp;
        int index;
        for (int i = 0; i < splits.length; i++) {
            if(splits[i].length()==0){
                continue;
            }
           ArrayList<Symbol> row = new ArrayList<>();
           String label = "";
           temp = splits[i].trim();
           String[] syms = temp.split(" ");

            for (int j = 0; j < syms.length ; j++) {
                // Non terminal
                if(syms[j].trim().startsWith("‘")){
                    index = syms[j].lastIndexOf("’");
                    if(index == -1){
                        System.err.println("Wrong Grammar");
                        exit(0);
                    }
                    label = syms[j].trim().substring(1,index);
                    sym = new Symbol(label);
                    row.add(sym);
                }
                else{
                    ArrayList<ArrayList<Symbol>> rightProd = new ArrayList<>();
                    label = syms[j].trim();
                    if(!symbols.containsKey(label)){
                        sym = new Symbol(label, rightProd);
                    }
                    else {
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

}
