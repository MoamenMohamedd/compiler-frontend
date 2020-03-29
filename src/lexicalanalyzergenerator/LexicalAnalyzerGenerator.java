package lexicalanalyzergenerator;

import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String key;
        String value;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while((line = bufferedReader.readLine())!=null){
                Stack s = new Stack();

                //Keywords
                if(line.startsWith("{")){
                    line=line.substring(1,line.length()-1);

                    String[] keywords = line.trim().split(" ");
                    for (int i = 0; i < keywords.length; i++) {
                        s.clear();
                        keywords[i] = keywords[i].replaceAll("", " ").trim();
                        s = getRegularExpression(keywords[i]);
                        nfas.put(keywords[i], buildNFA(s));
//                        System.out.println("Here at " + i + " " + keywords[i]);
                    }
                    continue;
                }

                //Punctuations
                if(line.startsWith("[")){
                    line=line.substring(1,line.length()-1);

                    String[] punctuations = line.trim().split(" ");
                    for (int i = 0; i < punctuations.length; i++) {
                        s.clear();
                        if(punctuations[i].startsWith("\\")){
                            punctuations[i] = punctuations[i].substring(1, punctuations[i].length());
                        }
                        s.push(punctuations[i]);
                        nfas.put(punctuations[i], buildNFA(s));
//                        System.out.println("Here at " + i + " " + punctuations[i]);
                    }
                    continue;
                }

                //Regular  expressions and regular definitions
                for (int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    // Regular expression
                    if(ch == ':'){
                        int index = i;
                        key = line.substring(0, index);
                        value = line.substring(index + 1, line.length()).trim();
                        Stack<String> regularExpression =  getRegularExpression(value);
                        if(key.equals("relop")){
                            for (int j = 0; j < regularExpression.size(); j++) {
                                String regex = regularExpression.get(j);
                                if(regex.equals("or")){
                                    s.push("or");
                                    continue;
                                }
                                regex = regex.replace("", " ").trim();
                                Stack temp = getRegularExpression(regex);
                                s.addAll(temp);
                            }
                            nfas.put(key, buildNFA(s));
                            continue;
                        }
                        nfas.put(key, buildNFA(regularExpression));
//                        System.out.println(key + "");
//                        System.out.println(value);
                    }
                }

                // Regular Definition
//                if(ch == '='){
//                    int index = i;
//                    key = line.substring(0, index).trim();
//                    value = line.substring(index + 1, line.length());
//                    System.out.println(key + "");
//                    System.out.println(value);
//
//                }
            }

            fileReader.close();    //closes the stream and release the resources
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
        System.out.println();
        System.out.println("STACK");
        System.out.println(stack);
        System.out.println("End Stack");
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


    // Regular expression
    private Stack<String> getRegularExpression(String expression){
        Stack<String> values = new Stack<>();
        String s;
        boolean flag = false;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            // Check for the brackets
            if(ch == '('){
                int closeindex = expression.lastIndexOf(')');
                if(closeindex == -1){
                    System.out.println("Error in input file");
                    System.exit(0);
                }
                //Check if there is no closure
                if(closeindex == expression.length()-1){
                    //Check if it the expression doesnt start with a bracket
                    //Concat the previous expression and the new one
                    if(i!=0){
                        s = expression.substring(0,i);
                        values.add("concat");
                        values.add(s);
                    }
                    expression = expression.substring(i+1,closeindex).trim();
                    i = -1;
                    continue;
                }
                char closure = expression.charAt(closeindex+1);
                //Kleene closure on the bracket
                if(closure == '*'){
                    values.add("kleene");
                    expression = expression.substring(i+1,closeindex).trim();
                }
                //Positive closure on the bracket
                else if(closure == '+'){
                    values.add("positive");
                    expression = expression.substring(i+1,closeindex).trim();
                }
                i = -1;
                continue;
            }
            //The or regex
            if(ch == '|'){
                s = expression.substring(0, i);
                expression = expression.substring(i+1,expression.length()).trim();    //starting after the or character
                values.add("or");
                if(s.length()!=0){
                    values.add(s);
                }
                i = -1;
                continue;
            }

            //positive closure
            if(ch == '+'){
                if(flag == false) {
                    s = expression.substring(0, i);
                    expression = expression.substring(i + 1, expression.length()).trim();
                    values.add("positive");
                    values.add(s);
                    i = -1;
                    continue;
                }
                flag = false;
            }

            //kleene closure
            if(ch == '*'){
                if(flag == false) {
                    s = expression.substring(0, i);
                    expression = expression.substring(i + 1, expression.length()).trim();
                    values.add("kleene");
                    values.add(s);
                    i = -1;
                    continue;
                }
                flag = false;
            }

            // Concat
            if(ch==' '){

                s = expression.substring(0, i);
                String temp = expression.substring(i,expression.length()).trim();
                //check if the concat is between two expressions
                if(temp.startsWith("|") || temp.startsWith("+") || temp.startsWith("*")){
                    continue;
                }
                expression = expression.substring(i,expression.length()).trim();
                values.add("concat");
                values.add(s);
                i = -1;
                continue;
            }

            if(ch == '\\'){
                char nextchar = expression.charAt(i+1);
                if(nextchar == 'L'){
                    expression = "epsilon" + expression.substring(i+2,expression.length());
                    i = -1;
                    continue;
                }
                else if(nextchar == '*' || nextchar == '+'){
                    flag = true;
                }
                expression = expression.substring(0, i) + expression.substring(i+1,expression.length());
                expression = expression.trim();
                i = -1;
            }
        }

        values.add(expression);
//        System.out.println(values);
        return values;
    }


}
