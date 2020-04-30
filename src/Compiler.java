import lexicalanalyzergenerator.*;
import parsergenerator.Parser;
import parsergenerator.ParserGenerator;

public class Compiler {
    public static void main(String[] args) {
        LexicalAnalyzerGenerator lexicalAnalyzerGenerator = new LexicalAnalyzerGenerator(System.getProperty("user.dir") + "/src" + "//input-rules-2.txt");
        ParserGenerator parserGenerator = new ParserGenerator(System.getProperty("user.dir") + "/src" + "//input-rules-2.txt");


        LexicalAnalyzer lexicalAnalyzer = lexicalAnalyzerGenerator.getLexicalAnalyzer();
        lexicalAnalyzer.setInputProgram(System.getProperty("user.dir") + "/src" + "//input-program-2.txt");

        Parser parser = parserGenerator.getParser();
        parser.setLexicalAnalyzer(lexicalAnalyzer);


    }

}