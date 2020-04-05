package lexicalanalyzergenerator;

class Edge{
    private State from;
    private State to;
    private char input;

    public Edge(State from, State to, char input) {
        this.from = from;
        this.to = to;
        this.input = input;
    }

    public Edge(Edge edge) {
        this.from = edge.from;
        this.to = edge.to;
        this.input = edge.input;
    }

    public State getTo() {
        return to;
    }

    public void setTo(State to) {
        this.to = to;
    }

    public State getFrom() {
        return from;
    }

    public char getInput() {
        return input;
    }

    public void setFrom(State from) {
        this.from = from;
    }

    @Override
    public boolean equals(Object obj) {
        Edge other = (Edge)obj;

        return this.from.equals(other.from) &&
                this.to.equals(other.to) &&
                this.input == other.input;
    }

    @Override
    public String toString() {
        return  from.getLabel() + " " +  to.getLabel() + " " + (input == '~' ? "ep" : input);
    }
}