import lexicalanalyzergenerator.*;
import parsergenerator.Parser;
import parsergenerator.ParserGenerator;

public class Compiler {
    public static void main(String[] args) {
        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//lexicalanalyzergenerator//input-rules-4.txt");
        ParserGenerator parserGenerator = new ParserGenerator(System.getProperty("user.dir") + "/src" + "//parsergenerator//CFG-input.txt");


        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
        lexicalAnalyzer.setInputProgram(System.getProperty("user.dir") + "/src" + "//input-program-5.txt");

        Parser parser = parserGenerator.getParser();
        parser.setLexicalAnalyzer(lexicalAnalyzer);
        parser.parse();

    }

}
