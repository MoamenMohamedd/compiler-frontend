package lexicalanalyzergenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LexicalAnalyzerGenerator {
    private String pathToRules;

    public LexicalAnalyzerGenerator(String pathToRules) {
        this.pathToRules = pathToRules;
    }


    public LexicalAnalyzer getLexicalAnalyzer() {
        // Build NFAs
        Map<String, NFA> tokenNFAs = buildNFAs();

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
    private HashMap<String, NFA> buildNFAs() {
        Map<String, NFA> tokenNFAs = new HashMap<>();

        NFA nfa1 = new NFA('a');
        NFA nfa2 = new NFA('b');

//        NFA res = nfa1.concat(nfa2);
        NFA res = nfa1.or(nfa2);

        res.visualize();

        return null;
    }

    /**
     * Builds an NFA that matches a regular expression
     *
     * @param stack: Sequence of operations to do inorder to reach the NFA
     * @return NFA
     */
    private NFA buildNFA(Stack stack) {
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
