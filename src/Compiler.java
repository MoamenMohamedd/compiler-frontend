import lexicalanalyzergenerator.LexicalAnalyzer;
import lexicalanalyzergenerator.LexicalAnalyzerGenerator;

public class Compiler {
    public static void main(String[] args) {
        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "\\input.txt");
        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
        lexicalAnalyzer.setInputProgram("");

        lexicalAnalyzer.getNext();
    }
}
