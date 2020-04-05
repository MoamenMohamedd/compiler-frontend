import lexicalanalyzergenerator.*;

public class Compiler {
    public static void main(String[] args) {
//        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//input.txt");
//        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
//        lexicalAnalyzer.setInputProgram("");
//
//        lexicalAnalyzer.getNext();


        NFA nfa1 = new NFA('a');
        NFA nfa2 = new NFA('b');
        NFA res = nfa1.concat(nfa2);
        res.print();
        System.out.println("Start state: " + res.getStartState());
        System.out.println("Final state: " + res.getFinalStates().get(0));

    }
}
