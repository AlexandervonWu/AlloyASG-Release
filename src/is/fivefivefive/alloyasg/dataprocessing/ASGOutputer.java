package is.fivefivefive.alloyasg.dataprocessing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import parser.util.AlloyUtil;
import edu.mit.csail.sdg.parser.CompModule;
import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.asg.ASGraph;
import is.fivefivefive.alloyasg.asg.MagneticLaplacian;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.util.NodeUtil;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;
import parser.etc.Pair;

// This is the last processing step in Java; the rest is in Python
// Output the Classes and their corresponding IDs, equivalence maps, and the ASG in a complex Laplacian matrix
public class ASGOutputer {
    private static final double Q_VALUE = 0.25;
    private static void outputASGFile(ASGraph asg, DoubleMap<Integer, Node> nodeMap, Map<Integer, List<Integer>> equivMap, String targetDir, String filename) throws IOException {
        MagneticLaplacian lap = new MagneticLaplacian(asg, Q_VALUE);
        String repCsv = ""; // graph representation CSV
        for (int i = 0; i < asg.getNumVertices(); ++i) {
            Node n = nodeMap.get(i);
            Pair<List<Integer>, String> priorN = NodeUtil.initialVectorize(n, nodeMap);
            String name = priorN.b;
            List<Double> prior = NodeUtil.weight(priorN.a, NodeUtil.WEIGHT);
            for (int j = 0; j < prior.size(); ++j) {
                repCsv += prior.get(j) + ", ";
            }
            repCsv += name + "\n";
        }
        String equivCsv = ""; // equivalence CSV
        for (Integer i : equivMap.keySet()) {
            List<Integer> equiv = equivMap.get(i);
            equivCsv += i + ", ";
            for (Integer j : equiv) {
                equivCsv += j + ", ";
            }
            equivCsv += "\n";
        }
        String lapCsv = lap.toString();
        // save the CSV's AND the serializable Nodes
        File targetFldr = new File(targetDir);
        if (!targetFldr.exists()) {
            Files.createDirectories(Paths.get(targetDir)); 
        }
        Files.createDirectories(Paths.get(targetDir + "/" + filename));
        Files.createDirectories(Paths.get(targetDir + "/" + filename + "/nodes"));
        Files.write(Paths.get(targetDir + "/" + filename + "/representation.csv"), repCsv.getBytes());
        Files.write(Paths.get(targetDir + "/" + filename + "/equivalence.csv"), equivCsv.getBytes());
        Files.write(Paths.get(targetDir + "/" + filename + "/laplacian.csv"), lapCsv.getBytes());
    }

    public static void main(String[] args) throws IOException {
        String sourceDir = "AlloyParser/results/classified-data";
        String targetDir = "AlloyParser/results/ASGdata";
        File sourceFldr = new File(sourceDir);
        File[] label1 = sourceFldr.listFiles();
        for (File l1 : label1) {
            File[] label2 = l1.listFiles();
            for (File l2 : label2) {
                File[] models = l2.listFiles();
                for (File f : models) {
                    CompModule cm = AlloyUtil.compileAlloyModule(f.getAbsolutePath());
                    ModelUnit mu = new ModelUnit(null, cm);
                    ASGVisitor<Object> asgv = new ASGVisitor<Object>();
                    asgv.visit(mu, null);
                    asgv.AST2ASG();
                    ASGraph asg = asgv.getGraph();
                    DoubleMap<Integer, Node> nodeMap = asgv.getNodeMap();
                    Map<Integer, List<Integer>> equivMap = asgv.getEquivMap();
                    String localTargetDir = targetDir + "/" + l1.getName() + "/" + l2.getName();
                    outputASGFile(asg, nodeMap, equivMap, localTargetDir, f.getName());
                    asgv.saveNodeMap(localTargetDir + "/" + f.getName() + "/nodes", "");
                }
            }
        }
    }
}
