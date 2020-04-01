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
    private final int REG_DEF = 0;
    private final int REG_EXP = 1;
    private HashMap<String, NFA> keysToNFA = new HashMap<>();


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
            boolean deflag = false;

            while((line = bufferedReader.readLine())!=null){
                Stack s = new Stack();

                //Keywords
                if(line.startsWith("{")){
                    line=line.substring(1,line.length()-1);

                    String[] keywords = line.trim().split(" ");
                    for (int i = 0; i < keywords.length; i++) {
                        s.clear();
                        keywords[i] = keywords[i].replaceAll("", " ").trim().replaceAll(" ", "");
                        s = getRegularExpression(keywords[i]);
//                        System.out.println("KEYWORDSS");
                        nfas.put(keywords[i], buildNFA(s, REG_EXP, ""));
//                        System.out.println("Here at " + i + " " + keywords[i]);
                    }
                    continue;
                }

                //Punctuations
                if(line.startsWith("[")){
                    line=line.substring(1,line.length()-1);

                    String[] punctuations = line.trim().split("(\\s\\\\|\\\\|\\s)");
                    for (int i = 0; i < punctuations.length; i++) {
                        s.clear();
                        if(punctuations[i].startsWith("\\")){
                            punctuations[i] = punctuations[i].substring(1, punctuations[i].length());
                        }
                        s.push(punctuations[i]);
//                        System.out.println("Puncctt");
                        nfas.put(punctuations[i], buildNFA(s, REG_EXP, ""));
                    }
                    continue;
                }
                char prev = line.charAt(0);
                //Regular  expressions and regular definitions
                for (int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    // Regular expression
                    if(ch == ':'){
                        deflag = true;
                        int index = i;
                        key = line.substring(0, index);
                        value = line.substring(index + 1, line.length()).trim();
                        Stack<String> regularExpression =  getRegularExpression(value);
                        if(key.equals("relop")){
                            for (int j = 0; j < regularExpression.size(); j++) {
                                String regex = regularExpression.get(j);
                                if(regex.equals("or")||regex.length()==1||regex.equals("concat")){
                                    s.push(regex);
                                    continue;
                                }
                                regex = regex.replace("", " ").trim();
                                Stack temp = getRegularExpression(regex);
                                s.addAll(temp);
                            }
                            nfas.put(key, buildNFA(s, REG_EXP, ""));
                            continue;
                        }
                        nfas.put(key, buildNFA(regularExpression, REG_EXP, ""));
                        break;
                    }

                    // Regular Definition
                    if(ch == '=' && prev!='\\' && deflag == false){
                        System.out.println("DEFINITION");
                        int index = i;
                        key = line.substring(0, index).trim();
                        value = line.substring(index + 1, line.length()).trim();
                        Stack<String> regularDefinition =  getRegularExpression(value);
                        nfas.put(key, buildNFA(regularDefinition, REG_DEF, key));
                        break;
                    }
                }
                deflag = false;
            }
            System.out.println(nfas.toString());
//            System.out.println(nfas.get("digit").ge);
            fileReader.close();    //closes the stream and release the resources
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private NFA buildNFA(Stack stack, int mode, String key) throws Exception {

//        Stack<NFA> builder = new Stack<>();
//        System.out.println();
        System.out.println();
        System.out.println("STACK");
        System.out.println(stack);
        System.out.println("End Stack");

        NFA nfa;

        /* Regular Definition */
        if (mode == REG_DEF) {

            char op1, op2;
            op1 = stack.pop().toString().charAt(0);
            op2 = stack.pop().toString().charAt(0);
            nfa = new NFA(op1);

            while (!stack.empty()) {
                String operation = stack.pop().toString();

                /* or operation */
                if (operation.equalsIgnoreCase("or")) {
                    nfa.or(new NFA(op2));
                }

                /* concat operation */
                else if (operation.equalsIgnoreCase("concat")) {
                    nfa.concat(new NFA(op2));
                }

                /* kleene closure operation */
                else if (operation.equalsIgnoreCase("kleene")) {
                    nfa.kleeneClosure();
                }

                /* Extract the next operand from the stack if exists */
                if (!stack.empty()) {
                    op2 = stack.pop().toString().charAt(0);
                }
            }
            keysToNFA.put(key, nfa);
        }

        /* Regular Expression */
        else {

            String op1, op2, operation;
            op1 = stack.pop().toString();


            if (keysToNFA.containsKey(op1))
            {
                nfa = (NFA)keysToNFA.get(op1).clone();
            }
            else
            {
                nfa = new NFA(op1.charAt(0));
            }

            if (stack.empty()) {
                return nfa;
            }

            op2 = stack.pop().toString();

            while (!stack.empty()) {

                operation = stack.pop().toString();

                /* or operation */
                if (operation.equalsIgnoreCase("or")) {

                    if (keysToNFA.containsKey(op2))
                    {
                        nfa.or((NFA)keysToNFA.get(op2).clone());
                    }

                    else
                    {
                        nfa.or(new NFA(op2.charAt(0)));
                    }

                }

                /* concat operation */
                else if (operation.equalsIgnoreCase("concat")) {

                    if (keysToNFA.containsKey(op2))
                    {
                        nfa.concat((NFA)keysToNFA.get(op2).clone());
                    }

                    else
                    {
                        nfa.concat(new NFA(op2.charAt(0)));
                    }

                }

                /* kleene closure operation */
                else if (operation.equalsIgnoreCase("kleene")) {
                    nfa.kleeneClosure();
                }

                /* Extract the next operand from the stack if exists */
                if (!stack.empty()) {
                    op2 = stack.pop().toString();
                }
            }
        }

        return nfa;
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
        boolean operator = false;

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

            //Extra closing bracket
            if(ch==')'){
                System.err.println("Error in rules");
                System.exit(0);
            }

            //The or regex
            if(ch == '|'){

                s = expression.substring(0, i);
                expression = expression.substring(i+1,expression.length()).trim();    //starting after the or character
                values.add("or");
                if(s.length()!=0){
                    if(flag){
                        String[] temp = s.split("");
                        values.add("concat");
                        for (int j = 0; j < temp.length; j++) {
                            values.add(temp[j]);
                        }
                        flag = false;
                        i = -1;
                        continue;
                    }
                    values.add(s);
                }
                i = -1;
                continue;
            }

            //positive closure
            if(ch == '+'){
                if(!flag) {
                    s = expression.substring(0, i);
                    expression = expression.substring(i + 1, expression.length()).trim();
                    //check if there is anything after the positive closure
                    if(expression.length()!=0) {
                        if (expression.startsWith("|")) {
                            values.add("or");
                            expression = expression.substring(1, expression.length()).trim();
                        } else {
                            values.add("concat");
                        }
                    }
                    values.add("positive");
                    values.add(s);
                    i = -1;
                    continue;
                }
            }

            //kleene closure
            if(ch == '*'){
                if(!flag) {
                    s = expression.substring(0, i);
                    expression = expression.substring(i + 1, expression.length()).trim();
                    if(expression !="") {
                        if (expression.startsWith("|")) {
                            values.add("or");
                            expression = expression.substring(1, expression.length()).trim();
                        } else {
                            values.add("concat");
                        }
                    }
                    values.add("kleene");
                    values.add(s);
                    i = -1;
                    continue;
                }
            }

            // Range
            if(ch == '-'){
                if(!flag) {
                    char start = expression.charAt(0);
                    expression = expression.substring(i+1,expression.length()).trim();
                    char end = expression.charAt(0);
                    String newexpression = "";
                    for (int j = (byte)start; j < (byte)end; j++) {
                        newexpression = newexpression + (char)j + "|";
                    }
                    newexpression = newexpression + end;
                    expression = newexpression + expression.substring(1,expression.length());
                    i = -1;
                    continue;
                }
            }

            // Concat
            if(ch==' '){
                // <\= | <   ---> < = | <
                s = expression.substring(0, i);
                String temp = expression.substring(i,expression.length()).trim();
                //check if the concat is between two expressions
                if(temp.startsWith("|") || temp.startsWith("+") || temp.startsWith("*") || temp.startsWith("-")){
                    expression = expression.substring(0,i) + expression.substring(i+1,expression.length());
                    i--;
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
                    expression = "~" + expression.substring(i+2,expression.length());
                    i = -1;
                    continue;
                }
                else if(nextchar == '*' || nextchar == '+' || nextchar == '-' || nextchar == '=' || nextchar == ':'){
                    flag = true;
                }
                expression = expression.substring(0, i)  + expression.substring(i+1,expression.length());
                expression = expression.trim();
                i --;
                continue;
            }

            if(flag){
//                System.out.println("EXPRESSION");
//                System.out.println("INDEX           "  + i);
//                System.out.println(expression);
                if(i > 0){
//                    System.out.println(expression.substring(0,i));
//                    System.out.println("AA");
//                    System.out.println(expression.substring(i,i+1));
                    s = expression.substring(0,i).trim();
                    String op = expression.substring(i,i+1);
                    String temp = expression.substring(i+1, expression.length()).trim();
                    if(temp.startsWith("|")){
                        continue;
                    }
                    expression = expression.substring(i+1, expression.length()).trim();
                    values.add("concat");
                    values.add(s);
                    values.add(op);

                }
                flag = false;
            }
        }
        if(expression.length()!=0){
            values.add(expression);
        }
//        System.out.println(values);
        return values;
    }


}
