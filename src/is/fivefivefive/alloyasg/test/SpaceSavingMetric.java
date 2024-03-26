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
import is.fivefivefive.alloyasg.exceptions.ScopeNotFoundException;
import is.fivefivefive.alloyasg.exceptions.UnsupportedConstantException;
import is.fivefivefive.alloyasg.vector.Vector2D;


public class SpaceSavingMetric {
    private static double computeSpaceSaving(ASGVisitor<Object> bigAsg, Predicate pred) 
            throws ScopeNotFoundException, UnsupportedConstantException {
        ExprOrFormula rootExpr = pred.getBody().getBodyExpr();
        ASGVisitor<Object> asg = new ASGVisitor<Object>();
        asg.visit(rootExpr, null);
        int sizeAst = asg.getGraph().getNumVertices() - asg.countNOOP;
        Pair<List<Double>, List<double[]>> pair0 = PredSubgraph.exploreGraph(bigAsg, rootExpr);
        Pair<List<Double>, Vector2D> adjPair = PredSubgraph.createCompactGraph(pair0.a, pair0.b);
        Vector2D adj = adjPair.b;
        int sizeCompact = adj.size();
        double spaceSaving = ((sizeAst - sizeCompact) / (double) (sizeAst + 1));
        return spaceSaving;
    }

    public static Pair<Double, Double> computeSpaceSaving(String modelDirectory)
            throws ScopeNotFoundException, UnsupportedConstantException {
        CompModule module = AlloyUtil.compileAlloyModule(modelDirectory);
        ModelUnit mu = new ModelUnit(null, module);
        List<Node> lstNodes = ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "inv", false);
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "Inv", false));
        lstNodes.addAll(ASTNodeFinder.findNodesByTypeAndName(mu, Predicate.class, "prop", false));
        ASGVisitor<Object> asg0 = new ASGVisitor<Object>();
        asg0.visit(mu, null);
        ASGVisitor<Object> asg1 = new ASGVisitor<Object>();
        ExprOrFormula root0 = ((Predicate)lstNodes.get(0)).getBody().getBodyExpr();
        asg1.visit(root0, null);
        ASGVisitor<Object> asg2 = new ASGVisitor<Object>();
        ExprOrFormula root1 = ((Predicate)lstNodes.get(1)).getBody().getBodyExpr();
        asg2.visit(root1, null);
        double spaceSaving0 = computeSpaceSaving(asg0, (Predicate)lstNodes.get(0));
        double spaceSaving1 = computeSpaceSaving(asg0, (Predicate)lstNodes.get(1));
        return Pair.of(spaceSaving0, spaceSaving1);
    }

    public static void main(String[] args) throws ScopeNotFoundException, UnsupportedConstantException {
        // String dir = args[0];
        String dir = "data/";
        File directory = new File(dir);
        File[] label1 = directory.listFiles();
        String toBePrinted = "Label, #Models, Relation, Node Reduced for the mutant model%, Node Reduced for the ground truth%\n";
        int totalCount = 0;
        double totalAvg = 0.0;
        for (File l1 : label1) {
            String labelProblem = l1.getName();
            File[] label2 = l1.listFiles();
            for (File l2 : label2) {
                String labelStatus = l2.getName();
                switch (labelStatus) {
                    case "OVERCONSTRAINED":
                        labelStatus = "OVER";
                        break;
                    case "UNDERCONSTRAINED":
                        labelStatus = "UNDER";
                        break;
                    default:
                        
                }
                File[] models = l2.listFiles();
                double categorySumWrong = 0.0;
                double categorySumCorrect = 0.0;
                int countOfModels = 0;
                for (File f : models) {
                    String modelDirectory = f.getAbsolutePath();
                    try {
                        // System.out.println("begin: " + modelDirectory);
                        Pair<Double, Double> spaceSavingPair = computeSpaceSaving(modelDirectory);
                        categorySumWrong += spaceSavingPair.a;
                        categorySumCorrect += spaceSavingPair.b;
                        countOfModels++;
                        totalCount++;
                    } catch(Exception e) {
                        System.out.println("IN the error model: " + modelDirectory);
                        e.printStackTrace();
                        // throw e;
                    }

                }
                totalAvg += categorySumWrong + categorySumCorrect;
                categorySumWrong /= countOfModels;
                categorySumWrong = 100 * categorySumWrong;
                categorySumCorrect /= countOfModels;
                categorySumCorrect = 100 * categorySumCorrect;
                DecimalFormat df = new DecimalFormat("##.##");
                toBePrinted += (labelProblem + " & " + countOfModels + " & " + labelStatus + " & " + df.format(categorySumWrong) + " & " + df.format(categorySumCorrect) + " \\\\\n");
            }
        }
        totalAvg /= (totalCount*2);
        System.out.println(totalCount + ", " + totalAvg);
        System.out.println(toBePrinted);
    }
    
}
