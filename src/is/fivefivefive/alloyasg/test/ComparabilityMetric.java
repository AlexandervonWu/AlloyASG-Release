package is.fivefivefive.alloyasg.test;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import edu.mit.csail.sdg.parser.CompModule;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.ast.nodes.Predicate;
import parser.ast.visitor.ASTNodeFinder;
import parser.etc.Pair;
import parser.util.AlloyUtil;
import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.asg.PredSubgraph;
import is.fivefivefive.alloyasg.etc.Triple;
import is.fivefivefive.alloyasg.exceptions.ScopeNotFoundException;
import is.fivefivefive.alloyasg.exceptions.UnsupportedConstantException;
import is.fivefivefive.alloyasg.util.CompareUtil;
import is.fivefivefive.alloyasg.vector.Vector2D;
import is.fivefivefive.alloyasg.visualization.VisualAPI;


public class ComparabilityMetric {

    public static Pair<Triple<Integer, Integer, Integer>, String> computeComparability(String modelDirectory)
            throws ScopeNotFoundException, UnsupportedConstantException {
        CompModule module = AlloyUtil.compileAlloyModule(modelDirectory);
        ModelUnit mu = new ModelUnit(null, module);
        List<Node> lstNodes = ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "inv", false);
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "Inv", false));
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "prop", false));
        ASGVisitor<Object> asg0 = new ASGVisitor<Object>();
        asg0.visit(mu, null);
        ASGVisitor<Object> asg1 = new ASGVisitor<Object>();
        ExprOrFormula root1 = ((Predicate)lstNodes.get(1)).getBody().getBodyExpr(); // corrected
        asg1.visit(root1, null);
        ASGVisitor<Object> asg2 = new ASGVisitor<Object>();
        ExprOrFormula root2 = ((Predicate)lstNodes.get(0)).getBody().getBodyExpr(); // original
        asg2.visit(root2, null);
        Pair<List<Double>, Vector2D> pair1 = PredSubgraph.createCompactGraph(asg0, root1);
        Vector2D adj1 = pair1.b;
        Pair<List<Double>, Vector2D> pair2 = PredSubgraph.createCompactGraph(asg0, root2);
        Vector2D adj2 = pair2.b;

        Triple<Vector2D, Vector2D, List<Double>> comparability = CompareUtil.compareCSBASG(adj1, adj2, pair1.a, pair2.a);
        Vector2D adj1Compared = comparability.x;
        Vector2D adj2Compared = comparability.y;
        List<Double> allSignatures = comparability.z;
        return VisualAPI.getVisual(adj1Compared, adj2Compared, allSignatures, "RESULT_" + modelDirectory);
    }

    public static void main(String[] args) throws ScopeNotFoundException, UnsupportedConstantException {
        // String dir = args[0];
        String dir = "data/";
        File directory = new File(dir);
        File[] label1 = directory.listFiles();
        String toBePrinted = "Label, #Models, Relation, Correct%, Faulty%\n";
        int totalCount = 0;
        double totalAvgCor = 0.0;
        double totalAvgFau = 0.0;
        for (File l1 : label1) {
            String labelProblem = l1.getName();
            File[] label2 = l1.listFiles();
            for (File l2 : label2) {
                String labelStatus = l2.getName();
                double lsAvgCorrect = 0.0;
                double lsAvgFaulty = 0.0;
                switch (l2.getName()) {
                    case "OVERCONSTRAINED":
                        labelStatus = "OVER";
                        break;
                    case "UNDERCONSTRAINED":
                        labelStatus = "UNDER";
                        break;
                    default:
                        
                }
                File[] models = l2.listFiles();
                int countOfModels = 0;
                for (File f : models) {
                    String modelDirectory = f.getAbsolutePath();
                    try {
                        // System.out.println("begin: " + modelDirectory);
                        Pair<Triple<Integer, Integer, Integer>, String> comparabilityPair = computeComparability(modelDirectory);
                        Triple<Integer, Integer, Integer> comparability = comparabilityPair.a;
                        int correctOnly = comparability.x;
                        int faultyOnly = comparability.y;
                        int both = comparability.z;
                        double percentageCorrect = 1.0 * both / (correctOnly + both);
                        double percentageFaulty = 1.0 * both / (faultyOnly + both);
                        /* if (Double.isNaN(percentageFaulty)) {
                            throw new RuntimeException("");
                        }*/
                        lsAvgCorrect += percentageCorrect;
                        lsAvgFaulty += percentageFaulty;
                        // System.out.println(visual);
                        countOfModels++;
                        totalCount++;
                    } catch(Exception e) {
                        System.out.println("IN the error model: " + modelDirectory);
                        // File deadmodel = new File(modelDirectory);
                        // deadmodel.delete();
                        e.printStackTrace();
                        // throw e;
                    }

                }
                totalAvgCor += lsAvgCorrect;
                totalAvgFau += lsAvgFaulty;
                lsAvgCorrect /= countOfModels;
                lsAvgFaulty /= countOfModels;
                lsAvgCorrect = 100 * lsAvgCorrect;
                lsAvgFaulty = 100 * lsAvgFaulty;
                DecimalFormat df = new DecimalFormat("##.##");
                toBePrinted += (labelProblem + " & " + countOfModels + " & " + labelStatus + " & " + df.format(lsAvgCorrect) + " & " + df.format(lsAvgFaulty) + " \\\\\n");
                // toBePrinted += (labelProblem + " & " + countOfModels + " & " + labelStatus + " & " + df.format(categorySumWrong) + " & " + df.format(categorySumCorrect) + " \\\\\n");
            }
        }
        totalAvgCor /= totalCount;
        totalAvgFau /= totalCount;
        System.out.println(totalCount + ", " + totalAvgFau + ", " + totalAvgCor);
        System.out.println(toBePrinted);
    }
    
}
