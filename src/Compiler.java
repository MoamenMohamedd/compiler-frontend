import lexicalanalyzergenerator.*;

public class Compiler {
    public static void main(String[] args) {
        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//input.txt");
        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
        lexicalAnalyzer.setInputProgram(System.getProperty("user.dir") + "/src" + "//input-1.txt");

//        while (lexicalAnalyzer.hasNext())
//            System.out.println(lexicalAnalyzer.getNext());

    }
}