package parsergenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

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

//        leftFactorGrammar();

//        eliminateLeftRecursion();

        calculateFirstSets();

//        calculateFollowSets();

        firstSets.entrySet().forEach((entry) -> {
            System.out.println("Label: " + entry.getKey().getLabel() + ", First Set: " + entry.getValue().toString());
        });

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
        HashSet<Symbol> seen = new HashSet<>();

        for(Symbol symbol : grammar)
        {
            if (!seen.contains(symbol.getLabel()))
            {
                seen.add(symbol);
                firstSets.put(symbol, new HashSet<>());
                firstSet(symbol, seen);
            }
        }
    }

    private void firstSet(Symbol symbol, HashSet<Symbol> seen)
    {
        /*  If symbol is terminal first(symbol) = {symbol} */
        if (symbol.isTerminal() || symbol.getLabel().equals("\\L"))
        {
            /* Add the non-terminal or epsilon to the first sets */
            firstSets.put(symbol, new HashSet<String>(){{
                add(symbol.getLabel());
            }});

            return;
        }


        ArrayList<ArrayList<Symbol>> productionRule = symbol.getProductions();
        ArrayList<Symbol> firstChild = new ArrayList<>();

        /* Get the first symbol in the rule */
        productionRule.forEach((rule) -> {

            Symbol child = null;
            for (int i = 0; i < rule.size(); i++)
            {
                child = rule.get(i);

                /* if the child first not computed before */
                if (!seen.contains(child))
                {
                    seen.add(child);
                    firstSet(child, seen);
                }

                /* Add the child first to the parent first */
                for (String val : firstSets.get(child))
                {
                    if (!firstSets.containsKey(symbol))
                    {
                        firstSets.put(symbol, new HashSet<String>(){{
                            add(val);
                        }});
                    }
                    else
                    {
                        firstSets.get(symbol).add(val);
                    }
                }

                /* If the child contains epsilon and not the right most symbol so remove it and continue through the next symbols */
                if (firstSets.get(symbol).contains("\\L") && i != rule.size() - 1)
                {
                    firstSets.get(symbol).remove("\\L");
                }
                /* Child doesn't contains epsilon or it is the last symbol */
                else {
                    break;
                }
            }
        });
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
