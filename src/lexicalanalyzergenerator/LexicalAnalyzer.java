package lexicalanalyzergenerator;

public class LexicalAnalyzer {
    private DFA dfa;

    public LexicalAnalyzer(DFA dfa) {
        this.dfa = dfa;
    }

    public void setInputProgram(String pathToProgram){

    }

    /**
     * Gets next token from input program
     *
     * @return String
     */
    public String getNext(){
        return null;
    }

    /**
     * Checks if there is a next token from
     * input program
     *
     * @return boolean
     */
    public boolean hasNext(){
        return false;
    }
}
