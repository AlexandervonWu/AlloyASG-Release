package is.fivefivefive.alloyasg.test;
import java.util.List;

import edu.mit.csail.sdg.parser.CompModule;
import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.asg.PredSubgraph;
import is.fivefivefive.alloyasg.asg.SimpleGraph;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.etc.Triple;
import is.fivefivefive.alloyasg.exceptions.ScopeNotFoundException;
import is.fivefivefive.alloyasg.exceptions.UnsupportedConstantException;
import is.fivefivefive.alloyasg.representations.NodeRepresentation;
import is.fivefivefive.alloyasg.util.CompareUtil;
import is.fivefivefive.alloyasg.vector.Vector2D;
import is.fivefivefive.alloyasg.visualization.VisualAPI;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Predicate;
import parser.ast.visitor.ASTNodeFinder;
import parser.etc.Pair;
import parser.util.AlloyUtil;

public class ComparabilityCaseStudy {

    public static void main(String[] args) throws ScopeNotFoundException, UnsupportedConstantException {
        long startTime = System.currentTimeMillis();
        String file;
        if (args.length == 0) {
            file = "example.als";
        } else {
            file = args[0];
        }
        System.out.println("Processing " + file);
        CompModule module = AlloyUtil.compileAlloyModule(file);
        ModelUnit mu = new ModelUnit(null, module);
        List<Node> lstNodes = ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "inv", false);
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "Inv", false));
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "prop", false));
        System.out.println(lstNodes);
        ASGVisitor<Object> asg0 = new ASGVisitor<Object>();
        asg0.visit(mu, null);
        ExprOrFormula rootExpr1 = ((Predicate)lstNodes.get(0)).getBody().getBodyExpr();
        ASGVisitor<Object> asg1 = new ASGVisitor<Object>();
        asg1.visit(rootExpr1, null);
        Pair<List<Double>, List<double[]>> pair0 = PredSubgraph.exploreGraph(asg0, rootExpr1);
        Pair<List<Double>, Vector2D> adjacencyPair = PredSubgraph.createCompactGraph(pair0.a, pair0.b);
        Vector2D adjacency1 = adjacencyPair.b;
        List<Double> signatures1 = adjacencyPair.a;
        System.out.println("The faulty model has " + signatures1.size() + " distinct thetas.");
        System.out.println("The syntactic and semantic components are given below: ");
        System.out.println("-----------------------------------------------");
        int i = 0;
        for (double signature : signatures1) {
            double sigUnit = signature / NodeRepresentation.UNIT;
            int syntactic = (int) Math.round(sigUnit);
            int semantic = (int) Math.round((sigUnit - syntactic) * 128);
            if (syntactic == -2) {
                semantic = (int) Math.round((sigUnit - syntactic) * 65536); 
            }
            System.out.println(i + " " + syntactic + " " + semantic);
            i++;
        }
        System.out.println("-----------------------------------------------");
        System.out.println("The adjacency matrix is given below: ");
        System.out.println("-----------------------------------------------");
        System.out.println(adjacency1);
        System.out.println("-----------------------------------------------");
        System.out.println("Back-translating of the adjacency matrix gives: ");
        System.out.println("-----------------------------------------------");
        SimpleGraph sg1 = new SimpleGraph(adjacency1, signatures1, asg0.getSigList(), asg0.getScopeRelMap(), new DoubleMap<>(asg0.getFieldMap()), asg0.getCallableList(), asg0.numRels());
        System.out.println(sg1.backTranslate(0));
        System.out.println("-----------------------------------------------");
        System.out.println("Visualizing the adjacency matrix gives: ");
        System.out.println("-----------------------------------------------");
        System.out.println(VisualAPI.getVisual(adjacency1, signatures1, "Faulty_Predicate"));
        System.out.println("-----------------------------------------------");
        System.out.println("Copy the command above and run it in the terminal to visualize the graph.");
        System.out.println("-----------------------------------------------");
        ExprOrFormula rootExpr2 = ((Predicate)lstNodes.get(1)).getBody().getBodyExpr();
        ASGVisitor<Object> asg2 = new ASGVisitor<Object>();
        asg2.visit(rootExpr2, null);
        Pair<List<Double>, List<double[]>> pair1 = PredSubgraph.exploreGraph(asg0, rootExpr2);
        Pair<List<Double>, Vector2D> adjacencyPair2 = PredSubgraph.createCompactGraph(pair1.a, pair1.b);
        Vector2D adjacency2 = adjacencyPair2.b;
        List<Double> signatures2 = adjacencyPair2.a;
        System.out.println("The corrected model has " + signatures2.size() + " distinct thetas.");
        System.out.println("The syntactic and semantic components are given below: ");
        System.out.println("-----------------------------------------------");
        i = 0;
        for (double signature : signatures2) {
            double sigUnit = signature / NodeRepresentation.UNIT;
            int syntactic = (int) Math.round(sigUnit);
            int semantic = (int) Math.round((sigUnit - syntactic) * 128);
            if (syntactic == -2) {
                semantic = (int) Math.round((sigUnit - syntactic) * 65536); 
            }
            System.out.println(i + " " + syntactic + " " + semantic);
            i++;
        }
        System.out.println("-----------------------------------------------");
        System.out.println("The adjacency matrix is given below: ");
        System.out.println("-----------------------------------------------");
        System.out.println(adjacency2);
        System.out.println("-----------------------------------------------");
        System.out.println("Back-translating of the adjacency matrix gives: ");
        System.out.println("-----------------------------------------------");
        SimpleGraph sg2 = new SimpleGraph(adjacency2, signatures2, asg0.getSigList(), asg0.getScopeRelMap(), new DoubleMap<>(asg0.getFieldMap()), asg0.getCallableList(), asg0.numRels());
        System.out.println(sg2.backTranslate(0));
        System.out.println("-----------------------------------------------");
        System.out.println("Visualizing the adjacency matrix gives: ");
        System.out.println("-----------------------------------------------");
        System.out.println(VisualAPI.getVisual(adjacency2, signatures2, "Corrected_Predicate"));
        System.out.println("-----------------------------------------------");
        System.out.println("Copy the command above and run it in the terminal to visualize the graph.");
        System.out.println("-----------------------------------------------");
        System.out.println("Comparing the two predicates, gives a colored graph as shown below: ");
        System.out.println("-----------------------------------------------");
        Triple<Vector2D, Vector2D, List<Double>> compareTriple = CompareUtil.compareCSBASG(adjacency2, adjacency1, signatures2, signatures1);
        Vector2D adjacency1C = compareTriple.x;
        Vector2D adjacency2C = compareTriple.y;
        List<Double> signaturesC = compareTriple.z;
        System.out.println(VisualAPI.getVisual(adjacency1C, adjacency2C, signaturesC, "Compare"));
        System.out.println("-----------------------------------------------");
        System.out.println("Copy the command above and run it in the terminal to visualize the graph.");
        System.out.println("Blue edges signify that the edge is present in the corrected predicate, but not in the faulty predicate.");
        System.out.println("Red edges signify that the edge is present in the faulty predicate, but not in the corrected predicate.");
        System.out.println("Black edges signify that the edge is present in both the predicates.");
        System.out.println("-----------------------------------------------");
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken: " + (endTime - startTime) + " ms");
        System.out.println("END OF COMPARABILITY CASE STUDY");
    }
}
