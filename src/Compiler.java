import lexicalanalyzergenerator.*;
import parsergenerator.Parser;
import parsergenerator.ParserGenerator;

public class Compiler {
    public static void main(String[] args) {
        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//input-rules-3.txt");
        ParserGenerator parserGenerator = new ParserGenerator(System.getProperty("user.dir") + "/src" + "//CFG-input.txt");


        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
        lexicalAnalyzer.setInputProgram(System.getProperty("user.dir") + "/src" + "//input.txt");

        Parser parser = parserGenerator.getParser();
       parser.setLexicalAnalyzer(lexicalAnalyzer);
       parser.parse();

    }

}