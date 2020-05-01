package parsergenerator;

import lexicalanalyzergenerator.LexicalAnalyzer;

public class Parser {
    private ParseTable parseTable;
    private LexicalAnalyzer lexicalAnalyzer;

    public Parser(ParseTable parseTable) {
        this.parseTable = parseTable;
    }

    public void setLexicalAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    // Parse
}
