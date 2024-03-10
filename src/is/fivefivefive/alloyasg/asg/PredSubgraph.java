package is.fivefivefive.alloyasg.asg;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import parser.ast.nodes.ExprOrFormula;
import parser.etc.Pair;
import parser.ast.nodes.Node;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.UnaryExpr;

import com.abdulfatir.jcomplexnumber.ComplexNumber;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.etc.Triple;
import is.fivefivefive.alloyasg.exceptions.ScopeNotFoundException;
import is.fivefivefive.alloyasg.exceptions.UnsupportedConstantException;
import is.fivefivefive.alloyasg.vector.Vector2D;
import is.fivefivefive.alloyasg.representations.NodeRepresentation;
import is.fivefivefive.alloyasg.util.ODUtil;

public class PredSubgraph {

    public static final int ORDER_DIVISOR = 17;
    public static final double DIFFERENTIATOR = 1e-8;

    private static boolean isNOOP(Node node) {
        return (node instanceof UnaryExpr &&
            (((UnaryExpr) node).getOp() == parser.ast.nodes.UnaryExpr.UnaryOp.NOOP));
    }
    /**
     * Explore the graph and create a compact graph.
     * 
     * @param asgv the ASGVisitor to use
     * @param root the root of the ExprOrFormula tree
     * @return the list of distinct thetas and the adjacency matrix of the compact
     *         graph
     * @throws ScopeNotFoundException
     * @throws UnsupportedConstantException
     */
    public static Pair<List<Double>, List<double[]>> exploreGraph(ASGVisitor<Object> asgv, ExprOrFormula root)
            throws ScopeNotFoundException, UnsupportedConstantException {
        if (isNOOP(root)) {
            return exploreGraph(asgv, (ExprOrFormula)root.getChildren().get(0));
        }
        List<Double> thetas = new ArrayList<Double>();
        Map<Double, Integer> thetaCounter = new HashMap<Double, Integer>();
        ASGVisitor<Object> smallAsgv = new ASGVisitor<Object>();
        List<double[]> compAdjacency = new ArrayList<double[]>();
        smallAsgv.visit(root, null);
        // smallAsgv.simplify();
        ASGraph smallAsg = smallAsgv.getGraph();
        int numVertices = smallAsg.getNumVertices();
        DoubleMap<Integer, Node> smallNodeMap = smallAsgv.getNodeMap();
        int i = 0;
        while (i < numVertices) {
            Node node = smallNodeMap.get(i);
            boolean noop = (node instanceof UnaryExpr &&
                    (((UnaryExpr) node).getOp() == parser.ast.nodes.UnaryExpr.UnaryOp.NOOP));
            if (noop || !((node instanceof ExprOrFormula) || node instanceof RelDecl)) {
                // remove other non-ExprOrFormula or VarDecl Nodes
                // since the only relevant non-ExprOrFormula Nodes are VarDecls
                // which are already enclosed in the larger AsgVisitor's scope
                Node parent = node.getParent();
                while (!smallNodeMap.containsValue(parent)) {
                    parent = parent.getParent();
                    if (parent == null) {
                        throw new RuntimeException();
                    }
                }
                int parentId = smallNodeMap.rget(parent);
                List<Node> children = node.getChildren();
                int underNodes = children.size();
                if (underNodes > 1) {
                    throw new RuntimeException("Node " + node.getClass() + " has more than one child.");
                }
                for (Node child : node.getChildren()) {
                    int childId = smallNodeMap.rget(child);
                    smallAsg.addEdge(parentId, childId, smallAsg.getEdgeWeight(parentId, i));
                }
                smallAsg.removeVertex(i);
                smallNodeMap.remove(i);
                for (int j = i; j < smallNodeMap.size(); ++j) {
                    Node temp = smallNodeMap.get(j + 1);
                    smallNodeMap.remove(j + 1);
                    smallNodeMap.put(j, temp);
                }
                numVertices--;
                continue;
            } else i++;
        }
        i = 0; numVertices = smallAsg.getNumVertices();
        while (i < numVertices) {
            Node node = smallNodeMap.get(i);

            // if (node instanceof ExprOrFormula || node instanceof RelDecl) {
            // if ((node instanceof ExprOrFormula && !noop) || node instanceof RelDecl) {
            NodeRepresentation rep = new NodeRepresentation(asgv, node);
            int p = ODUtil.getMaxChildrenBySyntax(rep.getSyntacticRepresentation());
            double[] adjRow = (double[]) smallAsg.getRow(i); // outer edges
            double sign = rep.fineGrainSignature();
            if (thetas.contains(sign)) {
                thetas.add(sign);
                thetaCounter.put(sign, thetaCounter.get(sign) + 1);
            } else {
                thetas.add(sign);
                thetaCounter.put(sign, 0);
            }
            for (int j = 0; j < adjRow.length; ++j) {
                if (adjRow[j] != 0) {
                    adjRow[j] = Math.pow(2, thetaCounter.get(sign) * p + adjRow[j] - 1);
                }
            }
            compAdjacency.add(adjRow);
            ++i;
        }
        return Pair.of(thetas, compAdjacency);
    }

    /**
     * Create a compact graph from the list of distinct thetas and the adjacency
     * matrix of the compact graph.
     * 
     * @param thetas        the list of distinct thetas
     * @param compAdjacency the adjacency matrix of the compact graph
     * @return the list of distinct thetas and the adjacency matrix of the compact
     *         graph
     * @throws IllegalArgumentException
     */
    public static Pair<List<Double>, Vector2D> createCompactGraph(List<Double> thetas, List<double[]> compAdjacency)
            throws IllegalArgumentException {
        if (compAdjacency.size() != thetas.size()) {
            throw new IllegalArgumentException(
                    "The number of vertices in the graph and the number of thetas must be the same; " +
                            "got " + compAdjacency.size() + " and " + thetas.size() + " respectively.");
        }
        int n = compAdjacency.size();
        List<Triple<Double, Double, Double>> edges = new ArrayList<Triple<Double, Double, Double>>();
        List<Double> distinctSignatures = new ArrayList<Double>();
        for (int i = 0; i < n; ++i) {
            double theta_i = thetas.get(i);
            if (!distinctSignatures.contains(theta_i)) {
                distinctSignatures.add(theta_i);
            }
            for (int j = 0; j < n; ++j) {

                double theta_j = thetas.get(j);
                double edge = compAdjacency.get(i)[j];
                if (edge > 0) {
                    // if (theta_i == theta_j) System.out.println("theta_i == theta_j at " + i + ",
                    // " + j + " with edge " + edge);
                    edges.add(new Triple<Double, Double, Double>(theta_i, theta_j, edge));
                }
            }
        }
        int nprime = distinctSignatures.size();
        /*
         * Map<Triple<Double, Double, Double>, Integer> edgeOrdinalTracker = new
         * HashMap<Triple<Double, Double, Double>, Integer>();
         * for (Triple<Double, Double, Double> edge : edges) {
         * if (!edgeOrdinalTracker.containsKey(edge)) {
         * edgeOrdinalTracker.put(edge, 1);
         * } else {
         * edgeOrdinalTracker.put(edge, edgeOrdinalTracker.get(edge) + 1);
         * }
         * }
         */
        // use polynomial to represent the execution order and the multiplicity of an
        // edge
        // OBSOLETE: use DIFFERENTIATOR to differentiate between the execution order of
        // an edge
        Vector2D adjacency = new Vector2D(nprime);
        for (int i = 0; i < nprime; ++i) {
            for (Triple<Double, Double, Double> edge : edges) {
                if (Math.abs(edge.x - distinctSignatures.get(i)) < DIFFERENTIATOR) {
                    int j = distinctSignatures.indexOf(edge.y);
                    double z = edge.z;
                    // edgeOrdinalTracker.put(edge, edgeOrdinalTracker.get(edge) - 1);
                    ComplexNumber current = adjacency.get(i, j);
                    // OBSOLETE: for each multiedge, add the last edge weight as the constant,
                    // multiply the original value by the order divisor p==17
                    // current.multiply(ORDER_DIVISOR);
                    // z = Math.pow(2, counterOrdinalTracker *
                    // ODUtil.getMaxChildrenBySyntax(syntactic) + z);
                    adjacency.set(i, j, ComplexNumber.add(
                            current,
                            new ComplexNumber(z * Math.cos(thetas.get(i) - thetas.get(j)),
                                    z * Math.sin(thetas.get(i) - thetas.get(j)))));
                }
            }
        }
        /*
         * Vector2D diagonal = new Vector2D(nprime);
         * for (int i = 0; i < nprime; ++i) {
         * double current = 0;
         * for (int j = 0; j < nprime; ++j) {
         * current += adjacency.get(i, j).mod();
         * }
         * diagonal.set(i, i, new ComplexNumber(current, 0));
         * }
         * Vector2D laplacian = adjacency.subtract(diagonal);
         */
        return Pair.of(distinctSignatures, adjacency);
    }

    public static Pair<List<Double>, Vector2D> createCompactGraph(ASGVisitor<Object> asgv, ExprOrFormula root)
            throws ScopeNotFoundException, UnsupportedConstantException {
        Pair<List<Double>, List<double[]>> pair = exploreGraph(asgv, root);
        return createCompactGraph(pair.a, pair.b);
    }
}
