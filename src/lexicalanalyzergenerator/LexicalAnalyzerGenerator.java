package lexicalanalyzergenerator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LexicalAnalyzerGenerator {
    private String pathToRules;

    public LexicalAnalyzerGenerator(String pathToRules) {
        this.pathToRules = pathToRules;
    }


    public LexicalAnalyzer getLexicalAnalyzer() {
        System.out.println(this.pathToRules);
        File file = new File(this.pathToRules);

        // Build NFAs
        Map<String, NFA> tokenNFAs = buildNFAs(file);

        // Combine NFAs
        NFA combinedNFA = combineNFAs(tokenNFAs);

        // Convert combined NFA to DFA
        DFA dfa = convertToDFA(combinedNFA);

        // Minimize DFA
        DFA minimizedDFA = minimizeDFA(dfa);

        return new LexicalAnalyzer(minimizedDFA);
    }

    /**
     * Reads input file (lexical rules) and
     * builds all needed NFAs
     *
     * @return Map of key value pairs. The key denotes the token that this NFA matches
     * while the value denotes the NFA
     */
    private HashMap<String, NFA> buildNFAs(File file) {
        HashMap<String, NFA> nfas = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine())!=null){
                Stack st = new Stack();

                //Keywords
                if(line.startsWith("{")){
                    line=line.substring(1,line.length()-1);

                    String[] keywords = line.trim().split(" ");
                    for (int i = 0; i < keywords.length; i++) {
                        Stack s = new Stack();
                        s.push(keywords[i]);
                        nfas.put(keywords[i], buildNFA(s));
                        System.out.println("Here at " + i + " " + keywords[i]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, NFA> tokenNFAs = new HashMap<>();
        return null;
    }

    /**
     * Builds an NFA that matches a regular expression
     *
     * @param stack: Sequence of operations to do inorder to reach the NFA
     * @return NFA
     */
    private NFA buildNFA(Stack stack) {
        System.out.println("STACK");
        System.out.println(stack);
        return null;
    }


    /**
     * Combines all NFAs
     *
     * @param tokensNFA: Map of key value pairs. The key denotes the token that this NFA matches
     *                   while the value denotes the NFA
     * @return
     */
    private NFA combineNFAs(Map<String, NFA> tokensNFA) {
        return null;
    }

    private DFA convertToDFA(NFA nfa) {
        return null;
    }

    private DFA minimizeDFA(DFA dfa) {

        return null;
    }
}
