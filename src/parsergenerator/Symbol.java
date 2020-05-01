package parsergenerator;

import java.util.ArrayList;

/**
 * This class represents a symbol -> terminal | non terminal
 */
public class Symbol {
    private boolean isTerminal;
    private String label;
    private ArrayList<ArrayList<Symbol>> productions;

    public Symbol(String label) {
        this.label = label;
        this.isTerminal = true;
    }

    public Symbol(String label, ArrayList<ArrayList<Symbol>> productions) {
        this.label = label;
        this.productions = productions;
        this.isTerminal = false;
    }

    /**
     * If its productions can't be further left factored
     * it returns arraylist with current non terminal
     * otherwise it left factors the productions and creates new
     * non terminals as needed and returns them in the array list
     *
     * @return ArrayList<Symbol>
     */
    public ArrayList<Symbol> leftFactorProductions() {

        return null;
    }

    /**
     * Returns all terminals (labels) in this non terminal's productions
     *
     * @return ArrayList<String>
     */
    public ArrayList<String> getTerminals() {
        return null;
    }

    public void setProductions(ArrayList<ArrayList<Symbol>> productions){ this.productions = productions;}

    public boolean isTerminal() {
        return this.isTerminal;
    }

    public boolean isNonTerminal() {
        return !this.isTerminal;
    }


    @Override
    public String toString() {
        if (isTerminal)
            return label;
        else {
            StringBuilder builder = new StringBuilder();
            builder.append(label).append(" ::= ");
            for (ArrayList<Symbol> production : productions) {
                for (Symbol symbol : production) {
                    builder.append(symbol.label).append(" ");
                }
                builder.append("|");
            }

            builder.replace(builder.lastIndexOf("|"), builder.lastIndexOf("|"), "");
            return builder.toString();
        }
    }

}

