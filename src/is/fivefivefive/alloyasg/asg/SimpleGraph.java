package is.fivefivefive.alloyasg.asg;

import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.representations.NodeRepresentation;
import is.fivefivefive.alloyasg.util.ODUtil;
import is.fivefivefive.alloyasg.vector.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleGraph {
    private Vector2D graph;
    private List<Double> signatures;
    private List<String> sigList;
    private Map<Integer, Map<String, Integer>> dynamicScopeRelMap;
    private final Map<Integer, Map<String, Integer>> staticScopeRelMap;
    private DoubleMap<String, Map<String, Integer>> fieldMap;
    private List<String> callableList;
    private int numRels;
    private List<List<List<Double>>> edges;
    private Map<Integer, Integer> visited;

    /**
     * Constructor for a simple graph to be used for back translation.
     * 
     * @param graph        the adjacency matrix of the graph
     * @param signatures   the list of distinct angular signatures (so semantically
     *                     unique nodes)
     * @param sigList      the list of Alloy signatures
     * @param scopeRelMap  the map storing the relation declarations under each
     *                     scope
     * @param fieldMap     the map storing the fields under each signature
     * @param callableList the list of callable names
     * @param numRels      the total number of relations
     * 
     */
    public SimpleGraph(Vector2D graph, List<Double> signatures, List<String> sigList,
            Map<Integer, Map<String, Integer>> scopeRelMap,
            DoubleMap<String, Map<String, Integer>> fieldMap,
            List<String> callableList, int numRels) {
        this.graph = graph;
        this.signatures = signatures;
        this.sigList = sigList;
        this.staticScopeRelMap = scopeRelMap;
        this.dynamicScopeRelMap = new HashMap<Integer, Map<String, Integer>>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : scopeRelMap.entrySet()) {
            dynamicScopeRelMap.put(entry.getKey(), new HashMap<String, Integer>(entry.getValue()));
        }
        this.fieldMap = fieldMap;
        this.callableList = callableList;
        this.numRels = numRels;
        // first index is the source node, second index is the target node, third index
        // is the list of edges
        this.edges = new ArrayList<List<List<Double>>>();
        this.visited = new HashMap<Integer, Integer>();
        for (int i = 0; i < graph.size(); ++i) {
            edges.add(new ArrayList<List<Double>>());
            for (int j = 0; j < graph.size(); ++j) {
                edges.get(i).add(j, edgesFromAdj(i, j));
            }
            visited.put(i, 0);
        }

    }

    private List<Double> edgesFromAdj(int v1, int v2) {
        return edgesFromAdj(graph.get(v1, v2).getNorm());
    }

    // Find the edges from an adjacency matrix
    // ASSUMPTION: for the highest order, delta * p^N << 1
    private static List<Double> edgesFromAdj(double rel) {
        final int ORD_DIV = PredSubgraph.ORDER_DIVISOR;
        List<Double> edges = new ArrayList<Double>();
        int z = (int) (Math.round(rel));
        int delta = (int) (Math.round(rel - z) / PredSubgraph.DIFFERENTIATOR);
        while (z > 0) {
            double edgeRelation = (double) z % ORD_DIV;
            z = z / ORD_DIV;
            double edgeOrdinal = (double) delta % ORD_DIV;
            delta = delta / ORD_DIV;
            edges.add(edgeRelation + edgeOrdinal * PredSubgraph.DIFFERENTIATOR);
        }
        return edges;
    }

    // every time when find an edge, remove it from the graph
    // ord == inDegree is the beginning, the first exploration of a node
    /**
     * Back translate the graph to Alloy code.
     * 
     * @param root the graph node which the root of AST corresponds to.
     * @return the Alloy code
     */
    public String backTranslate(int root) {
        int order = visited.get(root);
        // System.out.println("Current node: " + root + ", order: " + order);
        visited.put(root, order + 1);

        double sigUnit = signatures.get(root) / NodeRepresentation.UNIT;
        int syntactic = (int) Math.round(sigUnit);
        int semantic = (int) Math.round((sigUnit - syntactic) * 128);
        // System.out.println("Syntactic: " + syntactic + ", semantic: " + semantic);
        Map<Integer, Map<Integer, Integer>> children = getChildren(root, graph,
                ODUtil.getMaxChildrenBySyntax(syntactic));
        // System.out.println("Children: " + children + ", syntactic: " + syntactic + ",
        // semantic: " + semantic);
        String compResult = "";
        // a back implementation of NodeRepresentation.fineGrainSignature())
        try {
            switch (syntactic) {
                case 1:
                    // SigExpr
                    return " " + sigList.get(semantic) + " ";
                case 2:
                    for (Map<String, Integer> scopeMap : staticScopeRelMap.values()) {
                        for (Map.Entry<String, Integer> entry : scopeMap.entrySet()) {
                            if (entry.getValue() == semantic) {
                                return " " + entry.getKey() + " ";
                            }
                        }
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                case -2:
                    // reserved for RelDecl under Qt's
                    int varDeclSemantic = (int) Math.round((sigUnit - syntactic) * 65536);
                    System.out.println(root +" " + varDeclSemantic);
                    boolean scopeFound = false;
                    Map<String, Integer> localScope = null;
                    int countOfRelsInDecl = 0;
                    List<Integer> varIds = new ArrayList<Integer>();
                    while (!scopeFound) {
                        int varId = varDeclSemantic % numRels;
                        varIds.add(varId);
                        System.out.println("VarId: " + varId);
                        System.out.println(dynamicScopeRelMap);
                        for (Map<String, Integer> scopeMap : dynamicScopeRelMap.values()) {
                            for (Map.Entry<String, Integer> entry : scopeMap.entrySet()) {
                                if (entry.getValue() == varId) {
                                    localScope = scopeMap;
                                    scopeFound = true;
                                    break;
                                }
                            }
                        }
                        countOfRelsInDecl++;
                        varDeclSemantic = varDeclSemantic / numRels;
                    }
                    while (varDeclSemantic > 0) {
                        countOfRelsInDecl++;
                        varDeclSemantic = varDeclSemantic / numRels;
                    }
                    compResult = "(";
                    // localScope should have variables with sequentially increasing ordinals
                    // remove the variables from the local scope once visited
                    // EDIT: ORDER NOT GUARANTEED
                    int varOrd = 0;
                    List<String> localScopeKeys = new ArrayList<String>(localScope.keySet());

                    for (String var : localScopeKeys) {
                        if (varOrd != 0) {
                            compResult += ",";
                        }
                        if (varIds.contains(localScope.get(var))) {
                            compResult += var;
                            varOrd++;
                            localScope.remove(var);
                        }
                        if (varOrd == countOfRelsInDecl) {
                            break;
                        }
                    }
                    compResult += ": ";
                    compResult += backTranslate(children.get(order).get(2));
                    compResult += ")";
                    // compResult += " | \n";
                    return compResult;
                case 3:
                    // for here the only thing needed is the name of the field
                    for (Map<String, Integer> field : fieldMap.values()) {
                        for (Map.Entry<String, Integer> entry : field.entrySet()) {
                            if (entry.getValue() == semantic) {
                                return " " + entry.getKey() + " ";
                            }
                        }
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                case 4:
                    // Integer constant
                    return " " + semantic + " ";
                case 5:
                    // Boolean constant or IDEN
                    if (semantic == 10) return "iden ";
                    else return " " + (semantic == 0 ? "false" : "true") + " ";
                case 6:
                case -6:
                    // callables
                    String callExprOrFormula = " (" + callableList.get(semantic) + "[";
                    for (int i = 0; i < children.get(order).size(); ++i) {
                        String arg = backTranslate(children.get(order).get(i));
                        if (i < children.get(order).size() - 1) {
                            callExprOrFormula += arg + ", ";
                        } else {
                            callExprOrFormula += arg + "]) ";
                        }
                    }
                    return callExprOrFormula;
                case 7:
                    // Unary Expressions
                    switch (semantic) {
                        case 1:
                            // set
                            return " (set " + backTranslate(children.get(order).get(1)) + ") ";
                        case 2:
                            // lone
                            return " (lone " + backTranslate(children.get(order).get(1)) + ") ";
                        case 3:
                            // one
                            return " (one " + backTranslate(children.get(order).get(1)) + ") ";
                        case 4:
                            // some
                            return " (some " + backTranslate(children.get(order).get(1)) + ") ";
                        case 5:
                            // exactlyof
                            return " (exactly " + backTranslate(children.get(order).get(1)) + ") ";
                        case 6:
                            // transpose
                            return " (~ " + backTranslate(children.get(order).get(1)) + ") ";
                        case 7:
                            // Rclosure
                            return " (* " + backTranslate(children.get(order).get(1)) + ") ";
                        case 8:
                            // closure
                            return " (^ " + backTranslate(children.get(order).get(1)) + ") ";
                        case 9:
                            // cardinality
                            return " (# " + backTranslate(children.get(order).get(1)) + ") ";
                        case 10:
                            // cast2int
                            return " (Int->int " + backTranslate(children.get(order).get(1)) + ") ";
                        case 11:
                            // cast2sigint
                            return " (int->Int " + backTranslate(children.get(order).get(1)) + ") ";
                        case 0:
                            // NOOP
                            return " (" + backTranslate(children.get(order).get(1)) + ") ";
                        case 12:
                            // prime
                            return " ( " + backTranslate(children.get(order).get(1)) + "') ";
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                case -7:
                    // Unary Formulae
                    switch (semantic) {
                        case 1:
                            // lone
                            return " (lone " + backTranslate(children.get(order).get(1)) + ") ";
                        case 2:
                            // one
                            return " (one " + backTranslate(children.get(order).get(1)) + ") ";
                        case 3:
                            // some
                            return " (some " + backTranslate(children.get(order).get(1)) + ") ";
                        case 4:
                            // no
                            return " (no " + backTranslate(children.get(order).get(1)) + ") ";
                        case 5:
                            // not
                            return " (!" + backTranslate(children.get(order).get(1)) + ") ";
                        case 6:
                            // before
                            return " (before " + backTranslate(children.get(order).get(1)) + ") ";
                        case 7:
                            // historically
                            return " (historically " + backTranslate(children.get(order).get(1)) + ") ";
                        case 8:
                            // once
                            return " (once " + backTranslate(children.get(order).get(1)) + ") ";
                        case 9:
                            // always
                            return " (always " + backTranslate(children.get(order).get(1)) + ") ";
                        case 10:
                            // eventually
                            return " (eventually " + backTranslate(children.get(order).get(1)) + ") ";
                        case 11:
                            // after
                            return " (after " + backTranslate(children.get(order).get(1)) + ") ";
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                case 8: {
                    // Binary Expressions
                    int left, right;
                    try {
                        left = children.get(order).get(1);
                        right = children.get(order).get(2);
                    } catch (Exception e) {
                        // the two children are the same
                        try {
                            left = children.get(order).get(2);
                            right = left;
                        } catch (Exception e1) {
                            System.out.println(children);
                            e1.printStackTrace();
                            throw e1;
                        }
                    }
                    switch (semantic) {
                        case 1:
                            // ARROW
                            return " (" + backTranslate(left) +
                                    "->" + backTranslate(right) + ") ";
                        case 2:
                            // ANY_ARROW_SOME
                            return " (" + backTranslate(left) +
                                    "->some " + backTranslate(right) + ") ";
                        case 3:
                            // ANY_ARROW_ONE
                            return " (" + backTranslate(left) +
                                    "->one " + backTranslate(right) + ") ";
                        case 4:
                            // ANY_ARROW_LONE
                            return " (" + backTranslate(left) +
                                    "->lone " + backTranslate(right) + ") ";
                        case 5:
                            // SOME_ARROW_ANY
                            return " (" + backTranslate(left) +
                                    "some-> " + backTranslate(right) + ") ";
                        case 6:
                            // SOME_ARROW_SOME
                            return " (" + backTranslate(left) +
                                    "some->some " + backTranslate(right) + ") ";
                        case 7:
                            // SOME_ARROW_ONE
                            return " (" + backTranslate(left) +
                                    "some->one " + backTranslate(right) + ") ";
                        case 8:
                            // SOME_ARROW_LONE
                            return " (" + backTranslate(left) +
                                    "some->lone " + backTranslate(right) + ") ";
                        case 9:
                            // ONE_ARROW_ANY
                            return " (" + backTranslate(left) +
                                    "one->" + backTranslate(right) + ") ";
                        case 10:
                            // ONE_ARROW_SOME
                            return " (" + backTranslate(left) +
                                    "one->some " + backTranslate(right) + ") ";
                        case 11:
                            // ONE_ARROW_ONE
                            return " (" + backTranslate(left) +
                                    "one->one " + backTranslate(right) + ") ";
                        case 12:
                            // ONE_ARROW_LONE
                            return " (" + backTranslate(left) +
                                    "one->lone" + backTranslate(right) + ") ";
                        case 13:
                            // LONE_ARROW_ANY
                            return " (" + backTranslate(left) +
                                    "lone->" + backTranslate(right) + ") ";
                        case 14:
                            // LONE_ARROW_SOME
                            return " (" + backTranslate(left) +
                                    "lone->some " + backTranslate(right) + ") ";
                        case 15:
                            // LONE_ARROW_ONE
                            return " (" + backTranslate(left) +
                                    "lone->one " + backTranslate(right) + ") ";
                        case 16:
                            // LONE_ARROW_LONE
                            return " (" + backTranslate(left) +
                                    "lone->lone " + backTranslate(right) + ") ";
                        case 17:
                            // ISSEQ_ARROW_LONE
                            return " (" + backTranslate(left) +
                                    "isSeq->lone " + backTranslate(right) + ") ";
                        case 18:
                            // JOIN
                            return " (" + backTranslate(left) +
                                    "." + backTranslate(right) + ") ";
                        case 19:
                            // DOMAIN
                            return " (" + backTranslate(left) +
                                    "<:" + backTranslate(right) + ") ";
                        case 20:
                            // RANGE
                            return " (" + backTranslate(left) +
                                    ":>" + backTranslate(right) + ") ";
                        case 21:
                            // INTERSECT
                            return " (" + backTranslate(left) +
                                    "&" + backTranslate(right) + ") ";
                        case 22:
                            // PLUSPLUS
                            return " (" + backTranslate(left) +
                                    "++" + backTranslate(right) + ") ";
                        case 23:
                            // PLUS
                            return " (" + backTranslate(left) +
                                    "+" + backTranslate(right) + ") ";
                        case 24:
                            // IPLUS
                            return " (" + backTranslate(left) +
                                    "@+" + backTranslate(right) + ") ";
                        case 25:
                            // MINUS
                            return " (" + backTranslate(left) +
                                    "-" + backTranslate(right) + ") ";
                        case 26:
                            // IMINUS
                            return " (" + backTranslate(left) +
                                    "@-" + backTranslate(right) + ") ";
                        case 27:
                            // MUL
                            return " (" + backTranslate(left) +
                                    "*" + backTranslate(right) + ") ";
                        case 28:
                            // DIV
                            return " (" + backTranslate(left) +
                                    "/" + backTranslate(right) + ") ";
                        case 29:
                            // REM
                            return " (" + backTranslate(left) +
                                    "%" + backTranslate(right) + ") ";
                        case 30:
                            // SHL
                            return " (" + backTranslate(left) +
                                    "<<" + backTranslate(right) + ") ";
                        case 31:
                            // SHA
                            return " (" + backTranslate(left) +
                                    ">>" + backTranslate(right) + ") ";
                        case 32:
                            // SHR
                            return " (" + backTranslate(left) +
                                    ">>>" + backTranslate(right) + ") ";
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                }
                case -8: {
                    // Binary Formulae
                    int left, right;
                    try {
                        left = children.get(order).get(1);
                        right = children.get(order).get(2);
                    } catch (Exception e) {
                        left = children.get(order).get(3);
                        right = left;
                    }
                    switch (semantic) {
                        case 1:
                            // EQUALS
                            return " (" + backTranslate(left) +
                                    "=" + backTranslate(right) + ") ";
                        case 2:
                            // NOT_EQUALS
                            return " (" + backTranslate(left) +
                                    "!=" + backTranslate(right) + ") ";
                        case 3:
                            // IMPLIES
                            return " (" + backTranslate(left) +
                                    "=>" + backTranslate(right) + ") ";
                        case 4:
                            // LT
                            return " (" + backTranslate(left) +
                                    "<" + backTranslate(right) + ") ";
                        case 5:
                            // LTE
                            return " (" + backTranslate(left) +
                                    "<=" + backTranslate(right) + ") ";
                        case 6:
                            // GT
                            return " (" + backTranslate(left) +
                                    ">" + backTranslate(right) + ") ";
                        case 7:
                            // GTE
                            return " (" + backTranslate(left) +
                                    ">=" + backTranslate(right) + ") ";
                        case 8:
                            // NOT_LT
                            return " (" + backTranslate(left) +
                                    "!<" + backTranslate(right) + ") ";
                        case 9:
                            // NOT_LTE
                            return " (" + backTranslate(left) +
                                    "!<=" + backTranslate(right) + ") ";
                        case 10:
                            // NOT_GT
                            return " (" + backTranslate(left) +
                                    "!>" + backTranslate(right) + ") ";
                        case 11:
                            // NOT_GTE
                            return " (" + backTranslate(left) +
                                    "!>=" + backTranslate(right) + ") ";
                        case 12:
                            // IN
                            return " (" + backTranslate(left) +
                                    "in " + backTranslate(right) + ") ";
                        case 13:
                            // NOT_IN
                            return " (" + backTranslate(left) +
                                    "!in " + backTranslate(right) + ") ";
                        case 14:
                            // AND
                            return " (" + backTranslate(left) +
                                    "&&" + backTranslate(right) + ") ";
                        case 15:
                            // OR
                            return " (" + backTranslate(left) +
                                    "||" + backTranslate(right) + ") ";
                        case 16:
                            // IFF
                            return " (" + backTranslate(left) +
                                    "<=>" + backTranslate(right) + ") ";
                        case 17:
                            // UNTIL
                            return " (" + backTranslate(left) +
                                    "until " + backTranslate(right) + ") ";
                        case 18:
                            // RELEASES
                            return " (" + backTranslate(left) +
                                    "releases " + backTranslate(right) + ") ";
                        case 19:
                            // SINCE
                            return " (" + backTranslate(left) +
                                    "since " + backTranslate(right) + ") ";
                        case 20:
                            // TRIGGERED
                            return " (" + backTranslate(left) +
                                    "trigerred " + backTranslate(right) + ") ";
                    }
                    return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
                }
                case 9:
                    // ListExpr
                    if (semantic == 1) {
                        compResult = " (disjoint [";
                    } else {
                        compResult = " (pred/totalOrder [";
                    }
                    for (int i = 1; i <= children.get(order).size(); ++i) {
                        String arg = backTranslate(children.get(order).get(i));
                        if (i < children.get(order).size()) {
                            compResult += arg + ", ";
                        } else {
                            compResult += arg + "]) ";
                        }
                    }
                    return compResult;
                case -9:
                    // ListFormula
                    compResult = " (";
                    String opString;
                    if (semantic == 1) {
                        opString = " && ";
                    } else {
                        opString = " || ";
                    }
                    for (int i = 1; i <= children.get(order).size(); ++i) {
                        String arg = backTranslate(children.get(order).get(i));
                        if (i < children.get(order).size()) {
                            compResult += arg + opString;
                        } else {
                            compResult += arg + ") ";
                        }
                    }
                    return compResult;
                case 10:
                    // LetExpr, letVar != letBound - no exponential needed
                    int letVar = children.get(order).get(1);
                    int letBound = children.get(order).get(2);
                    int letBody = children.get(order).get(3);
                    return " (let " + backTranslate(letVar) + " = ("
                            + backTranslate(letBound) + ") ("
                            + backTranslate(letBody) + ") ";
                case 11:
                    // QtExpr
                    for (int j = 2; j < children.get(order).size(); ++j) {
                        compResult += backTranslate(children.get(order).get(j));
                        if (j < children.get(order).size() - 1) {
                            compResult += ", ";
                        } else {
                            compResult += " | \n";
                        }
                    }
                    compResult += backTranslate(children.get(order).get(1)) + ") ";
                    if (semantic == 1) {
                        // sum
                        compResult = " ( " + compResult + ") ";
                    } else {
                        // comprehension
                        compResult = " { " + compResult + "} ";
                    }
                    return compResult;
                case -11:
                    // QtFormula
                    compResult = " (";
                    switch (semantic) {
                        case 1:
                            // all
                            compResult += "all ";
                            break;
                        case 2:
                            // no
                            compResult += "no ";
                            break;
                        case 3:
                            // lone
                            compResult += "lone ";
                            break;
                        case 4:
                            // one
                            compResult += "one ";
                            break;
                        case 5:
                            // some
                            compResult += "some ";
                            break;
                    }
                    // get the var declared here
                    for (int j = 2; j <= children.get(order).size(); ++j) {
                        String declString = backTranslate(children.get(order).get(j));
                        compResult += declString;
                        // System.out.println("Current: " + children.get(order).get(j) + ", with word "
                        // + declString);
                        if (j < children.get(order).size()) {
                            compResult += ", ";
                        } else {
                            compResult += " \n";
                        }
                    }
                    compResult += "{" + backTranslate(children.get(order).get(1)) + "}) ";
                    return compResult;
                case 12:
                case -12:
                    // ITE
                    return " (if " + backTranslate(children.get(order).get(1)) +
                            " then " + backTranslate(children.get(order).get(2)) +
                            " else " + backTranslate(children.get(order).get(3)) + ") ";
            }
            return "<UNKNOWN SEMANTIC " + semantic + " FOR SYNTAX ID " + syntactic + ">";
        } catch (Exception e) {
            System.out.println("Exception at node ID " + root + ", with " + syntactic + ", " + semantic);
            System.out.println("Order: " + order);
            System.out.println(compResult);
            throw e;
        }
    }

    // polynomial only
    private static Map<Integer, Map<Integer, Integer>> getChildren(int node, Vector2D graph, int od) {
        // First key is the order of the children
        Map<Integer, Map<Integer, Integer>> children = new HashMap<>();
        for (int i = 0; i < graph.size(); i++) { // i is the Node-Id of the child
            double zr = Math.round(graph.get(node, i).getNorm()); // the relation

            if (zr == 0) { // no relation
                continue;
            }
            // Find the degree of multiedges
            /*
             * int deg = 0;
             * double zrCopy = zr;
             * while (zrCopy >= 1) {
             * zrCopy = od == 1 ? zrCopy - 1 : zrCopy / od;
             * deg++;
             * }
             * for (int j = 0; j < deg; ++j) {
             */
            /*
             * int z = (int) Math.round(zr % od);
             * zr = od == 1 ? zr - 1 : zr / od;
             * if (z == 0) z = od;
             */
            // resolve Type 3 Confusion
            int z = (int) Math.round(zr);
            // if (od > 1) {
            // System.out.println("from node: " + node + ", to node: " + i + ", z-value: " +
            // z);
            int j = 0;
            while (z > 0) {
                int positionBool = z % 2;
                // System.out.println("to node: " + i + ", z-value: " + z + ", total times : " +
                // totalTimes + ", Position: " + position + ", PositionBool: " + positionBool);
                if (positionBool == 1) {
                    int times = j / od;
                    int omega = j % od + 1;
                    // System.out.println("times: " + times + ", omega: " + omega);
                    if (children.containsKey(times)) {
                        children.get(times).put(omega, i);
                    } else {
                        Map<Integer, Integer> child = new HashMap<>();
                        child.put(omega, i);
                        children.put(times, child);
                    }
                }
                z = z / 2;
                j++;
            }
            // } else {
            // order 1
            // for (int j = 0; j < z; ++j) {
            // Map<Integer, Integer> child = new HashMap<>();

            // }
            // }

        }
        return children;
    }

    public void reinitialize() {
        for (Map.Entry<Integer, Map<String, Integer>> entry : dynamicScopeRelMap.entrySet()) {
            dynamicScopeRelMap.put(entry.getKey(), new HashMap<String, Integer>(staticScopeRelMap.get(entry.getKey())));
        }
        visited = new HashMap<Integer, Integer>();
        for (int i = 0; i < graph.size(); ++i) {
            visited.put(i, 0);
        }
    }
    /*
     * private static int getInDegree(int node, Vector2D graph) {
     * int inDegree = 0;
     * for (int i = 0; i < graph.size(); i++) {
     * if (graph.get(i, node).getNorm() > 0) {
     * inDegree++;
     * }
     * }
     * return inDegree;
     * }
     * 
     * private static int getOutDegree(int node, Vector2D graph) {
     * int outDegree = 0;
     * for (int i = 0; i < graph.size(); i++) {
     * if (graph.get(node, i).getNorm() > 0) {
     * outDegree++;
     * }
     * }
     * return outDegree;
     * }
     */
}
