package lexicalanalyzergenerator;

import java.util.ArrayList;
import java.util.List;

class State {
    private int label;
    private List<Edge> edges;
    private boolean isStart;
    private boolean isFinal;
    private String token;

    public State(int label, boolean isStart, boolean isFinal) {
        this.label = label;
        this.isStart = isStart;
        this.isFinal = isFinal;
        edges = new ArrayList<>();
    }

    public void addEdge(State to, char input) {
        edges.add(new Edge(this, to, input));
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = new ArrayList<>(edges);
    }

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object obj) {
        return this.label == ((State) obj).label;
    }

    @Override
    public String toString() {
        return label + "";
    }
}
