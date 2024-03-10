package is.fivefivefive.alloyasg.asg;

public class ASGraph {
    private int[][] adjMatrix;
    private int numVertices;
    private int numEdges;
    
    public ASGraph(int numVertices) {
        this.numVertices = numVertices;
        this.numEdges = 0;
        this.adjMatrix = new int[numVertices][numVertices];
    }

    // Add a vertex to the graph
    public void addVertex() {
        int[][] newAdjMatrix = new int[this.numVertices + 1][this.numVertices + 1];
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                newAdjMatrix[i][j] = this.adjMatrix[i][j];
            }
        }
        this.adjMatrix = newAdjMatrix;
        this.numVertices++;
    }

    // Remove a vertex from the graph
    public void removeVertex(int v) {
        int[][] newAdjMatrix = new int[this.numVertices - 1][this.numVertices - 1];
        int flagi = 0, flagj = 0;
        this.numEdges -= this.getInDegree(v) + this.getOutDegree(v);
        for (int i = 0; i < this.numVertices; i++) {
            if (i == v) {
                flagi = 1;
                continue;
            }
            for (int j = 0; j < this.numVertices; j++) {
                if (j == v) {
                    flagj = 1;
                    continue;
                }
                newAdjMatrix[i - flagi][j - flagj] = this.adjMatrix[i][j];
            }
            flagj = 0;
        }
        this.adjMatrix = newAdjMatrix;
        this.numVertices--;
    }

    // Add an edge in the directed graph from vertex v1 to v2 with weight w
    public void addEdge(int v1, int v2, int w) {
        this.adjMatrix[v1][v2] = w;
        this.numEdges++;
    }

    // Remove an edge in the directed graph from vertex v1 to v2
    public void removeEdge(int v1, int v2) {
        this.adjMatrix[v1][v2] = 0;
        this.numEdges--;
    }

    // Get the number of vertices in the graph
    public int getNumVertices() {
        return this.numVertices;
    }

    // Get the number of edges in the graph
    public int getNumEdges() {
        return this.numEdges;
    }

    // Get the weight of the edge from vertex v1 to v2
    public int getEdgeWeight(int v1, int v2) {
        return this.adjMatrix[v1][v2];
    }

    // Get the neighbors of vertex v
    public int[] getNeighbors(int v) {
        int[] neighbors = new int[this.numVertices];
        int numNeighbors = 0;
        for (int i = 0; i < this.numVertices; i++) {
            if (this.adjMatrix[v][i] != 0) {
                neighbors[numNeighbors] = i;
                numNeighbors++;
            }
        }
        int[] result = new int[numNeighbors];
        for (int i = 0; i < numNeighbors; i++) {
            result[i] = neighbors[i];
        }
        return result;
    }

    // Get the in-degree of vertex v
    public int getInDegree(int v) {
        int inDegree = 0;
        for (int i = 0; i < this.numVertices; i++) {
            if (this.adjMatrix[i][v] != 0) {
                inDegree++;
            }
        }
        return inDegree;
    }

    // Get the out-degree of vertex v
    public int getOutDegree(int v) {
        int outDegree = 0;
        for (int i = 0; i < this.numVertices; i++) {
            if (this.adjMatrix[v][i] != 0) {
                outDegree++;
            }
        }
        return outDegree;
    }

    // Get the transpose of the graph
    public ASGraph getTranspose() {
        ASGraph transpose = new ASGraph(this.numVertices);
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                transpose.adjMatrix[i][j] = this.adjMatrix[j][i];
            }
        }
        return transpose;
    }

    // Print the graph
    public void printGraph() {
        System.out.println("Number of vertices: " + this.numVertices);
        System.out.println("Number of edges: " + this.numEdges);
        System.out.println("Adjacency matrix:");
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                System.out.print(this.adjMatrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    // Check if a vertex is leaf
    public boolean isLeaf(int v) {
        return this.getOutDegree(v) == 0;
    }

    // Find the parent of a vertex
    public int findParent(int v) {
        // in-degree of the vertex should be 1
        if (this.getInDegree(v) != 1) {
            return -1;
        }
        for (int i = 0; i < this.numVertices; i++) {
            if (this.adjMatrix[i][v] != 0) {
                return i;
            }
        }
        return -1;
    }

    // Combine two identical leaf vertices with different parents
    public void combineVertices(int v1, int v2) {
        // both v1 and v2 must be leaves
        if (!this.isLeaf(v1) || !this.isLeaf(v2)) {
            return;
        }
        // v2 must have in-degree 1
        if (this.getInDegree(v2) != 1) {
            return;
        }
        // v1 and v2 should have different parents
        int p1 = this.findParent(v1);
        int p2 = this.findParent(v2);
        if (p1 == p2) {
            return;
        }
        // combine v1 and v2
        this.addEdge(p2, v1, adjMatrix[p2][v2]);
        this.removeVertex(v2);
    }

    // Check if two graphs are isomorphic
    public static boolean isIsomorphic(ASGraph g1, ASGraph g2) {
        // number of vertices and edges must be the same
        if (g1.getNumVertices() != g2.getNumVertices() || g1.getNumEdges() != g2.getNumEdges()) {
            return false;
        }
        // in-degree and out-degree of each vertex must be the same
        for (int i = 0; i < g1.getNumVertices(); i++) {
            if (g1.getInDegree(i) != g2.getInDegree(i) || g1.getOutDegree(i) != g2.getOutDegree(i)) {
                return false;
            }
        }
        // check if the adjacency matrix is the same
        for (int i = 0; i < g1.getNumVertices(); i++) {
            for (int j = 0; j < g1.getNumVertices(); j++) {
                if (g1.getEdgeWeight(i, j) != g2.getEdgeWeight(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Get a specific row of the adjacency matrix
    public double[] getRow(int v) {
        double[] row = new double[this.numVertices];
        for (int i = 0; i < this.numVertices; i++) {
            row[i] = this.adjMatrix[v][i];
        }
        return row;
    }

    // Get a specific column of the adjacency matrix
    public double[] getColumn(int v) {
        double[] column = new double[this.numVertices];
        for (int i = 0; i < this.numVertices; i++) {
            column[i] = this.adjMatrix[i][v];
        }
        return column;
    }

}
