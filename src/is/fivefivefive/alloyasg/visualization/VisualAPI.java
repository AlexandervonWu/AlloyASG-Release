package is.fivefivefive.alloyasg.visualization;

import java.util.ArrayList;
import java.util.List;

import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.asg.ASGraph;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.etc.Triple;
import is.fivefivefive.alloyasg.representations.NodeRepresentation;
import is.fivefivefive.alloyasg.util.ODUtil;
import is.fivefivefive.alloyasg.vector.Vector2D;
import parser.ast.nodes.Body;
import parser.ast.nodes.Node;
import parser.ast.nodes.UnaryExpr;
import parser.etc.Pair;

public class VisualAPI {
    public static String visualizeAST(ASGVisitor<Object> asgv, String output) {
        String str = "echo \'digraph{";
        ASGraph ast = asgv.getGraph();
        DoubleMap<Integer, Node> nodeMap = asgv.getNodeMap();
        // remove noop

        int size = ast.getNumVertices();
        int i = 0;
        
        while (i < size) {
            Node node = nodeMap.get(i);
            boolean noop = (node instanceof UnaryExpr &&
                    (((UnaryExpr) node).getOp() == parser.ast.nodes.UnaryExpr.UnaryOp.NOOP));
            if (noop || node instanceof Body) {
                nodeMap.remove(i);
                int parentId = nodeMap.rget(node.getParent());
                int weight = ast.getEdgeWeight(parentId, i);
                int childId = nodeMap.rget(node.getChildren().get(0));
                ast.addEdge(parentId, childId, weight);
                for (int j = i + 1; j < size; ++j) {
                    if (nodeMap.get(j) != null) {
                        Node temp = nodeMap.get(j);
                        nodeMap.remove(j);
                        nodeMap.put(j - 1, temp);
                    }
                }
                ast.removeVertex(i);
                size--;
            } else {
                i++;
            }
        }
        // make AST
        for (i = 0; i < size; ++i) {
            String iname = nodeMap.get(i).getClass().getName().substring(17) + "_" + i;
            for (int j = 0; j < size; ++j) {
                int weight = ast.getEdgeWeight(i, j);
                if (weight > 0) {
                    String jname = nodeMap.get(j).getClass().getName().substring(17) + "_" + j;
                    str += "\"" + iname + "\"->\"" + jname + "\"[label=\"" + weight + "\",color=black];";
                }
            }
        }
        str += "}\' | dot -Tpng -o " + output + ".png";
        return str;
    }

    public static String getVisual(Vector2D graph, List<Double> signatures, String output) {
        int size = graph.size();
        String str = "echo \'digraph{";
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            nodes.add(getSemanticCategory(signatures.get(i)));
        }
        for (int i = 0; i < size; ++i) {
            int maxChildren = ODUtil.getMaxChildrenBySyntax((int) Math.round(signatures.get(i) / NodeRepresentation.UNIT));
            for (int j = 0; j < size; ++j) {
                List<Pair<Integer, Integer>> multiEdgeList = new ArrayList<>();
                int ev = (int) Math.round(graph.get(i, j).getNorm());
                if (ev > 0) {
                    int value = ev;
                    int k = 0;
                    while (value > 0) {
                        int positionBool = value % 2;
                        if (positionBool == 1) {
                            int times = k / maxChildren + 1;
                            int omega = -114514;
                            try {
                                omega = k % maxChildren + 1;
                            } catch (Exception e) {
                                System.out.println("in G: " + nodes.get(i) + " " + nodes.get(j));
                                e.printStackTrace();
                            }
                            multiEdgeList.add(Pair.of(times, omega));
                        }
                        value /= 2;
                        k++;
                    }
                }
                for (Pair<Integer, Integer> pair : multiEdgeList) {
                    str += "\"" + nodes.get(i) + "\"->\"" + nodes.get(j) + "\"[label=\"(" + pair.a + "," + pair.b + ")\",color=black];";
                }
            }
        }
        str += "}\' | dot -Tpng -o " + output + ".png";
        return str;
    }

    public static Pair<Triple<Integer, Integer, Integer>, String> getVisual(Vector2D graph1, Vector2D graph2, List<Double> signatures, String output) throws IllegalArgumentException {
        if (graph1.size() != graph2.size() || graph1.size() != signatures.size()) {
            throw new IllegalArgumentException("Graphs and signatures must be of the same size");
        }
        int size = graph1.size();
        int numberG1Only = 0, numberG2Only = 0, numberCommon = 0;
        String str = "echo \'digraph{";
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            nodes.add(getSemanticCategory(signatures.get(i)));
        }
        for (int i = 0; i < size; ++i) {
            int maxChildren = ODUtil.getMaxChildrenBySyntax((int) Math.round(signatures.get(i) / NodeRepresentation.UNIT));
            for (int j = 0; j < size; ++j) {
                List<Pair<Integer, Integer>> multiEdgeList1 = new ArrayList<>();
                List<Pair<Integer, Integer>> multiEdgeList2 = new ArrayList<>();
                int ev1 = (int) Math.round(graph1.get(i, j).getNorm());
                int ev2 = (int) Math.round(graph2.get(i, j).getNorm());
                if (ev1 > 0) {
                    int value = ev1;
                    int k = 0;
                    while (value > 0) {
                        int positionBool = value % 2;
                        if (positionBool == 1) {
                            int times = k / maxChildren + 1;
                            int omega = -114514;
                            try {
                                omega = k % maxChildren + 1;
                            } catch (Exception e) {
                                System.out.println("in G1: " + nodes.get(i) + " " + nodes.get(j));
                                e.printStackTrace();
                            }
                            multiEdgeList1.add(Pair.of(times, omega));
                        }
                        value /= 2;
                        k++;
                    }
                }
                if (ev2 > 0) {
                    int value = ev2;
                    int k = 0;
                    while (value > 0) {
                        int positionBool = value % 2;
                        if (positionBool == 1) {
                            int times = k / maxChildren + 1;
                            int omega = -114514;
                            try {
                                omega = k % maxChildren + 1;
                            } catch (Exception e) {
                                System.out.println("in G2: " + nodes.get(i) + " " + nodes.get(j));
                                e.printStackTrace();
                            }
                            multiEdgeList2.add(Pair.of(times, omega));
                        }
                        value /= 2;
                        k++;
                    }
                }
                for (Pair<Integer, Integer> pair : multiEdgeList1) {
                    boolean eqNode = false;
                    Pair<Integer, Integer> pairx = null;
                    for (Pair<Integer, Integer> pair2 : multiEdgeList2) {
                        if (pair.equals(pair2)) {
                            eqNode = true;
                            // black
                            str += "\"" + nodes.get(i) + "\"->\"" + nodes.get(j) + "\"[label=\"(" + pair.a + "," + pair.b + ")\",color=black];";
                            numberCommon++;
                            pairx = pair2;
                            break;
                        } /* else if (pair.b == pair2.b) {
                            eqNode = true;
                            // black, (o1, omega)|(o2, omega)
                            str += "\"" + nodes.get(i) + "\"->\"" + nodes.get(j) + "\"[label=\"(" + pair.a + "|" + pair2.a + "," + pair.b + ")\",color=black];";
                            numberCommon++;
                            pairx = pair2;
                            break;
                        }*/
                    }
                    if (eqNode) {
                        multiEdgeList2.remove(pairx);
                    } else {
                        // red
                        numberG1Only++;
                        str += "\"" + nodes.get(i) + "\"->\"" + nodes.get(j) + "\"[label=\"(" + pair.a + "," + pair.b + ")\",color=red];";
                    } 
                }
                for (Pair<Integer, Integer> pair : multiEdgeList2) {
                    numberG2Only++;
                    str += "\"" + nodes.get(i) + "\"->\"" + nodes.get(j) + "\"[label=\"(" + pair.a + "," + pair.b + ")\",color=blue];";
                }
            }
        }
        str += "}\' | dot -Tpng -o " + output + ".png";
        return Pair.of(new Triple<Integer, Integer, Integer>(numberCommon, numberG1Only, numberG2Only), str);
    }
    private static String getSemanticCategory(double signature) {
        double sigUnit = signature / NodeRepresentation.UNIT;
        int syntactic = (int) Math.round(sigUnit);
        int semantic = (int) Math.round((sigUnit - syntactic) * (syntactic == -2 ? 65536 : 128));
        switch (syntactic) {
            case 0:
                return "OverallRoot";
            case 1:
                return "SigExpr_ID_" + semantic;
            case 2:
                return "VarExpr_ID_" + semantic;
            case -2:
                return "VarDecl_ID_" + semantic;
            case 3:
                return "FieldExpr_ID_" + semantic;
            case 4:
                return "Constant_Int_" + semantic;
            case 5:
                return "Constant_" + (semantic == 10 ? "iden" : semantic == 1 ? "true" : "false");
            case 6:
                return "CallExpr_ID_" + semantic;
            case -6:
                return "CallFormula_ID_" + semantic;
            case 7:
                switch (semantic) {
                    case 0:
                        return "NOOP";
                    case 1:
                        return "UnaryExpr_SET";
                    case 2:
                        return "UnaryExpr_LONE";
                    case 3:
                        return "UnaryExpr_ONE";
                    case 4:
                        return "UnaryExpr_SOME";
                    case 5:
                        return "UnaryExpr_EXACTLYOF";
                    case 6:
                        return "UnaryExpr_TRANSPOSE";
                    case 7:
                        return "UnaryExpr_RCLOSURE";
                    case 8:
                        return "UnaryExpr_CLOSURE";
                    case 9:
                        return "UnaryExpr_CARDINALITY";
                    case 10:
                        return "UnaryExpr_CAST2INT";
                    case 11:
                        return "UnaryExpr_CAST2SIGINT";
                    case 12:
                        return "UnaryExpr_PRIME";
                }
            case -7:
                switch (semantic) {
                    case 1:
                        return "UnaryFormula_LONE";
                    case 2:
                        return "UnaryFormula_ONE";
                    case 3:
                        return "UnaryFormula_SOME";
                    case 4:
                        return "UnaryFormula_NO";
                    case 5:
                        return "UnaryFormula_NOT";
                    case 6:
                        return "UnaryFormula_BEFORE";
                    case 7:
                        return "UnaryFormula_HISTORICALLY";
                    case 8:
                        return "UnaryFormula_ONCE";
                    case 9:
                        return "UnaryFormula_ALWAYS";
                    case 10:
                        return "UnaryFormula_EVENTUALLY";
                    case 11:
                        return "UnaryFormula_AFTER";
                }
            case 8:
                switch (semantic) {
                    case 1:
                        return "BinaryExpr_ARROW";
                    case 2:
                        return "BinaryExpr_ANY_ARROW_SOME";
                    case 3:
                        return "BinaryExpr_ANY_ARROW_ONE";
                    case 4:
                        return "BinaryExpr_ANY_ARROW_LONE";
                    case 5:
                        return "BinaryExpr_SOME_ARROW_ANY";
                    case 6:
                        return "BinaryExpr_SOME_ARROW_SOME";
                    case 7:
                        return "BinaryExpr_SOME_ARROW_ONE";
                    case 8:
                        return "BinaryExpr_SOME_ARROW_LONE";
                    case 9:
                        return "BinaryExpr_ONE_ARROW_ANY";
                    case 10:
                        return "BinaryExpr_ONE_ARROW_SOME";
                    case 11:
                        return "BinaryExpr_ONE_ARROW_ONE";
                    case 12:
                        return "BinaryExpr_ONE_ARROW_LONE";
                    case 13:
                        return "BinaryExpr_LONE_ARROW_ANY";
                    case 14:
                        return "BinaryExpr_LONE_ARROW_SOME";
                    case 15:
                        return "BinaryExpr_LONE_ARROW_ONE";
                    case 16:
                        return "BinaryExpr_LONE_ARROW_LONE";
                    case 17:
                        return "BinaryExpr_ISSEQ_ARROW_LONE";
                    case 18:
                        return "BinaryExpr_JOIN";
                    case 19:
                        return "BinaryExpr_DOMAIN";
                    case 20:
                        return "BinaryExpr_RANGE";
                    case 21:
                        return "BinaryExpr_INTERSECT";
                    case 22:
                        return "BinaryExpr_PLUSPLUS";
                    case 23:
                        return "BinaryExpr_PLUS";
                    case 24:
                        return "BinaryExpr_IPLUS";
                    case 25:
                        return "BinaryExpr_MINUS";
                    case 26:
                        return "BinaryExpr_IMINUS";
                    case 27:
                        return "BinaryExpr_MUL";
                    case 28:
                        return "BinaryExpr_DIV";
                    case 29:
                        return "BinaryExpr_REM";
                    case 30:
                        return "BinaryExpr_SHL";
                    case 31:
                        return "BinaryExpr_SHA";
                    case 32:
                        return "BinaryExpr_SHR";
                }
            case -8:
                switch (semantic) {
                    case 1:
                        return "BinaryFormula_EQUALS";
                    case 2:
                        return "BinaryFormula_NOT_EQUALS";
                    case 3:
                        return "BinaryFormula_IMPLIES";
                    case 4:
                        return "BinaryFormula_LT";
                    case 5:
                        return "BinaryFormula_LTE";
                    case 6:
                        return "BinaryFormula_GT";
                    case 7:
                        return "BinaryFormula_GTE";
                    case 8:
                        return "BinaryFormula_NOT_LT";
                    case 9:
                        return "BinaryFormula_NOT_LTE";
                    case 10:
                        return "BinaryFormula_NOT_GT";
                    case 11:
                        return "BinaryFormula_NOT_GTE";
                    case 12:
                        return "BinaryFormula_IN";
                    case 13:
                        return "BinaryFormula_NOT_IN";
                    case 14:
                        return "BinaryFormula_AND";
                    case 15:
                        return "BinaryFormula_OR";
                    case 16:
                        return "BinaryFormula_IFF";
                    case 17:
                        return "BinaryFormula_UNTIL";
                    case 18:
                        return "BinaryFormula_RELEASES";
                    case 19:
                        return "BinaryFormula_SINCE";
                    case 20:
                        return "BinaryFormula_TRIGGERD";
                }
            case 9:
                return semantic == 1 ? "ListExpr_DISJOINT" : "ListExpr_TOTALORDER";
            case -9:
                return semantic == 1 ? "ListFormula_AND" : "ListFormula_OR";
            case 10:
                return "Let";
            case 11:
                return semantic == 1 ? "QtExpr_SUM" : "QtExpr_COMPREHENSION";
            case -11:
                switch (semantic) {
                    case 1:
                        return "QtFormula_ALL";
                    case 2:
                        return "QtFormula_NO";
                    case 3:
                        return "QtFormula_LONE";
                    case 4:
                        return "QtFormula_ONE";
                    case 5:
                        return "QtFormula_SOME";
                }
            case 12:
                return "ITEExpr";
            case -12:
                return "ITEFormula";
        }
        return "<UNKNOWN NODE>";
    }
}
 