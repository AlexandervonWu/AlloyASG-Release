package is.fivefivefive.alloyasg.asg;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.util.NodeUtil;

import java.util.HashMap;
import parser.ast.visitor.VoidVisitor;
import parser.etc.Pair;
import parser.ast.nodes.Node;
import parser.ast.nodes.Assertion;
import parser.ast.nodes.BinaryExpr;
// import parser.ast.nodes.BinaryExprOrFormula;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Body;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.Check;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Function;
import parser.ast.nodes.ITEExpr;
import parser.ast.nodes.ITEFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
// import parser.ast.nodes.ListExprOrFormula;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.ModuleDecl;
import parser.ast.nodes.OpenDecl;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.Run;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryExprOrFormula;
// import parser.ast.nodes.UnaryExprOrFormula;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/*
 * This class is used to visit the AST and build the ASGraph.
 * It is used to store the mapping between the node and its ID in the adjacency matrix.
 */
public class ASGVisitor<A> implements VoidVisitor<A> {

    private ASGraph graph = new ASGraph(0);    
    private DoubleMap<Integer, Node> nodeMap = new DoubleMap<Integer, Node>();
    private List<Node> addlNodes = new ArrayList<Node>();
    private int counter = -1;
    private Map<Integer, List<Integer>> equivMap = new HashMap<Integer, List<Integer>>();
    private List<String> sigList = new ArrayList<String>();
    private Map<Integer, Map<String, Integer>> scopeRelMap = new HashMap<Integer, Map<String, Integer>>();
    private Map<String, Map<String, Integer>> fieldMap = new HashMap<String, Map<String, Integer>>();
    private int globalRelCounter = 0, globalFieldCounter = 0;
    private List<String> callableList = new ArrayList<String>();
    private Map<String, String> subsigDict = new HashMap<String, String>();
    private Map<RelDecl, List<Integer>> relDeclList = new HashMap<RelDecl, List<Integer>>();
    public int countNOOP = 0;
    // Map<Node, Triple<String, Class<?>, Integer>> nodeTypeMap = new HashMap<Node, Triple<String, Class<?>, Integer>>();

    public ASGVisitor(){}

    public ASGraph getGraph() {
        return graph;
    }

    public DoubleMap<Integer, Node> getNodeMap() {
        return nodeMap;
    }

    public List<String> getSigList() {
        return sigList;
    }

    public List<String> getCallableList() {
        return callableList;
    }

    public Map<Integer, Map<String, Integer>> getScopeRelMap() {
        return scopeRelMap;
    }

    public Map<String, Map<String, Integer>> getFieldMap() {
        return fieldMap;
    }

    public Map<String, String> getSubsigDict() {
        return subsigDict;
    }

    public int numRels() {
        return globalRelCounter;
    }

    private void makeRoot(Node n) {
        counter++;
        graph.addVertex();
        nodeMap.put(graph.getNumVertices() - 1, n);
    }

    private void addIntoMapAndGraph(Node n, int parent, A arg, int edgeWeight) {
        if (n == null) {
            return;
        }
        counter++;
        graph.addVertex();
        nodeMap.put(counter, n);
        graph.addEdge(parent, counter, edgeWeight);
        n.accept(this, arg);
    }

    private void addIntoRelMap(RelDecl n) {
        if (!scopeRelMap.containsKey(nodeMap.rget(n.getParent()))) {
            scopeRelMap.put(nodeMap.rget(n.getParent()), new HashMap<String, Integer>());
        }
        List<Integer> id = null;
        for (RelDecl existing : relDeclList.keySet()) {
            if (NodeUtil.isomorphicRelDecl(existing, n)) {
                id = relDeclList.get(existing);
            }
        }
        if (id == null) {
            id = new ArrayList<Integer>();
            for (String name : n.getNames()) {
                scopeRelMap.get(nodeMap.rget(n.getParent())).put(name, globalRelCounter);
                id.add(globalRelCounter);
                globalRelCounter++;
            }
            relDeclList.put(n, id);
        } else {
            for (String name : n.getNames()) {
                scopeRelMap.get(nodeMap.rget(n.getParent())).put(name, id.get(n.getNames().indexOf(name)));
            }
        }
        
    }

    private void addIntoRelMap(LetExpr n) {
        if (!scopeRelMap.containsKey(nodeMap.rget(n))) {
            scopeRelMap.put(nodeMap.rget(n), new HashMap<String, Integer>());
        }
        Node var = n.getVar();
        if (var instanceof UnaryExpr) {
            scopeRelMap.get(nodeMap.rget(n)).put(((VarExpr) (var.getChildren().get(0))).getName(), globalRelCounter);
        } else {
            scopeRelMap.get(nodeMap.rget(n)).put(((VarExpr) var).getName(), globalRelCounter);
        }
        
        globalRelCounter++;
    }

    private void addIntoSubsigMap(SigDecl n) {
        String subsigName = n.getName();
        String parent = n.getParentName();
        subsigDict.put(subsigName, parent);
    }

    private void addIntoFieldMap(FieldDecl n) {
        // All field declarations has a parent of type SigDecl
        SigDecl parent = (SigDecl) n.getParent();
        String sigName = parent.getName();
        if (!fieldMap.containsKey(sigName)) {
            fieldMap.put(parent.getName(), new HashMap<String, Integer>());
        }
        for (String name : n.getNames()) {
            fieldMap.get(sigName).put(name, globalFieldCounter);
            globalFieldCounter++;
        }
    }

    // ModelUnit, have children ModuleDecl, OpenDecl, SigDecl, Predicate, Function, Assertion, Run, Check
    // ModuleDecl: 1, OpenDecl: 2, SigDecl: 3, Predicate: 4, Function: 5, Assertion: 6, Run: 7, Check: 8
    @Override
    public void visit(ModelUnit n, A arg) {
        makeRoot(n);
        addIntoMapAndGraph(n.getModuleDecl(), 0, arg, 1);
        for (OpenDecl o : n.getOpenDeclList()) {
            addIntoMapAndGraph(o, 0, arg, 2);
        }
        for (SigDecl s : n.getSigDeclList()) {
            addIntoMapAndGraph(s, 0, arg, 3);
        }
        for (Predicate p : n.getPredDeclList()) {
            addIntoMapAndGraph(p, 0, arg, 4);
        }
        for (Function f : n.getFunDeclList()) {
            addIntoMapAndGraph(f, 0, arg, 5);
        }
        for (Assertion a : n.getAssertDeclList()) {
            addIntoMapAndGraph(a, 0, arg, 6);
        }
        for(Run r : n.getRunCmdList()) {
            addIntoMapAndGraph(r, 0, arg, 7);
        }
        for(Check c : n.getCheckCmdList()) {
            addIntoMapAndGraph(c, 0, arg, 8);
        }
        // if (containsLet) System.out.println("parsing concluded with let");
        
    }

    @Override
    public void visit(ModuleDecl n, A arg) {
    }

    @Override
    public void visit(OpenDecl n, A arg) {
    }

    // SigDecl, have children FieldDecl and SigFact, FieldDecl: 1, SigFact: 2
    @Override
    public void visit(SigDecl n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        sigList.add(n.getName());
        int parent = nodeMap.rget(n);
        for (FieldDecl f : n.getFieldList()) {
            addIntoMapAndGraph(f, parent, arg, 1);
        }
        if (n.hasSigFact()) {
            addIntoMapAndGraph(n.getSigFact(), parent, arg, 2);
        }
        if (!n.isTopLevel()) {
            addIntoSubsigMap(n);
        }
    }

    // FieldDecl, have children ExprOrFormula as variable and ExprOrFormula as expression, variable: 1, expression: 2
    @Override
    public void visit(FieldDecl n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        addIntoFieldMap(n);
        ExprOrFormula expr = n.getExpr();
        if (expr != null) {
            if (expr instanceof UnaryExprOrFormula) {
                SigExpr csig = (SigExpr) expr.getChildren().get(0).getChildren().get(0);
                String cSigName = csig.getName();
                if (!fieldMap.containsKey(cSigName)) {
                    fieldMap.put(cSigName, new HashMap<String, Integer>());
                }
                for (String name : n.getNames()) {
                    fieldMap.get(cSigName).put("~" + name, globalFieldCounter);
                    globalFieldCounter++;
                }
            }

        }
        int parent = nodeMap.rget(n);
        for (ExprOrFormula v : n.getVariables()) {
            addIntoMapAndGraph(v, parent, arg, 1);
        }
        addIntoMapAndGraph(n.getExpr(), parent, arg, 2);
    }

    // ParamDecl, have children ExprOrFormula as variable and ExprOrFormula as expression, variable: 1, expression: 2
    @Override
    public void visit(ParamDecl n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        addIntoRelMap(n);
        int parent = nodeMap.rget(n);
        for (ExprOrFormula v : n.getVariables()) {
            addIntoMapAndGraph(v, parent, arg, 1);
        }
        addIntoMapAndGraph(n.getExpr(), parent, arg, 2);
    }

    // VarDecl, have children ExprOrFormula as variable and ExprOrFormula as expression, variable: 1, expression: 2
    @Override
    public void visit(VarDecl n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        addIntoRelMap(n);
        int parent = nodeMap.rget(n);
        for (ExprOrFormula v : n.getVariables()) {
            addIntoMapAndGraph(v, parent, arg, 1);
        }
        addIntoMapAndGraph(n.getExpr(), parent, arg, 2);
    }

    // ExprOrFormula, atomic
    @Override
    public void visit(ExprOrFormula n, A arg) {
        n.accept(this, arg);
    }

    @Override
    public void visit(SigExpr n, A arg) {
    }

    @Override
    public void visit(FieldExpr n, A arg) {
        // TODO: catch FieldExprs that are not in the fieldMap for a partial parsing
    }

    @Override
    public void visit(VarExpr n, A arg) {
    }

    // UnaryExpr, have children ExprOrFormula as expression, expression: 1
    @Override   
    public void visit(UnaryExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        if (n.getOp() == parser.ast.nodes.UnaryExpr.UnaryOp.NOOP) {
            countNOOP += 1;
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getSub(), parent, arg, 1);
    }

    // UnaryFormula, have children ExprOrFormula as expression, expression: 1
    @Override
    public void visit(UnaryFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }

        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getSub(), parent, arg, 1);
    }

    // BinaryExpr, have children ExprOrFormula as left and ExprOrFormula as right, left: 1, right: 2
    @Override
    public void visit(BinaryExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getLeft(), parent, arg, 1);
        addIntoMapAndGraph(n.getRight(), parent, arg, 2);
    }

    // BinaryFormula, have children ExprOrFormula as left and ExprOrFormula as right, left: 1, right: 2
    @Override
    public void visit(BinaryFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }

        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getLeft(), parent, arg, 1);
        addIntoMapAndGraph(n.getRight(), parent, arg, 2);
    }

    // ListExpr, have children ExprOrFormula as expression, expression: 1
    @Override
    public void visit(ListExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int i = 1;
        for (ExprOrFormula e : n.getArguments()) {
            addIntoMapAndGraph(e, parent, arg, i);
            i++;
        }
    }

    // ListFormula, have children ExprOrFormula as expression, expression: 1
    @Override
    public void visit(ListFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int i = 1;
        for (ExprOrFormula e : n.getArguments()) {
            addIntoMapAndGraph(e, parent, arg, i);
            i++;
        }
    }

    // CallExpr, have children ExprOrFormula as expression, expression: 1
    @Override
    public void visit(CallExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int i = 1;
        for (ExprOrFormula e : n.getArguments()) {
            addIntoMapAndGraph(e, parent, arg, i);
            i++;
        }
    }

    // CallFormula, have children ExprOrFormula as expression, expression: 1
    @Override
    public void visit(CallFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int i = 1;
        for (ExprOrFormula e : n.getArguments()) {
            addIntoMapAndGraph(e, parent, arg, i);
            i++;
        }
    }

    // QtExpr, have children VarDecl as variable and ExprOrFormula as body, variable: 1, expression: 2
    @Override
    public void visit(QtExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int j = 2;
        for (VarDecl v : n.getVarDecls()) {
            addIntoMapAndGraph(v, parent, arg, j);
            j++;
        }
        addIntoMapAndGraph(n.getBody(), parent, arg, 1);
    }

    // QtFormula, have children VarDecl as variable and ExprOrFormula as body, variable: 1, expression: 2
    @Override
    public void visit(QtFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        int j = 2;
        for (RelDecl v : n.getVarDecls()) {
            addIntoMapAndGraph(v, parent, arg, j);
            j++;
        }
        addIntoMapAndGraph(n.getBody(), parent, arg, 1);
    }

    // ITEExpr, have children ExprOrFormula as condition, ExprOrFormula as Then and ExprOrFormula as Else, condition: 1, left: 2, right: 3
    @Override
    public void visit(ITEExpr n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getCondition(), parent, arg, 1);
        addIntoMapAndGraph(n.getThenClause(), parent, arg, 2);  
        addIntoMapAndGraph(n.getElseClause(), parent, arg, 3);
    }

    // ITEFormula, have children ExprOrFormula as condition, ExprOrFormula as Then and ExprOrFormula as Else, condition: 1, left: 2, right: 3
    @Override
    public void visit(ITEFormula n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getCondition(), parent, arg, 1);
        addIntoMapAndGraph(n.getThenClause(), parent, arg, 2);  
        addIntoMapAndGraph(n.getElseClause(), parent, arg, 3);
    }

    // LetExpr, have children ExprOrFormula as variable, ExprOrFormula as bound and ExprOrFormula as body, variable: 1, bound: 2, expression: 3
    @Override
    public void visit(LetExpr n, A arg) {
        addIntoRelMap(n);
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getVar(), parent, arg, 1);
        addIntoMapAndGraph(n.getBound(), parent, arg, 2);
        addIntoMapAndGraph(n.getBody(), parent, arg, 3);
    }

    // ConstExpr
    @Override
    public void visit(ConstExpr n, A arg) {
    }

    // Body, have children ExprOrFormula as body, body: 1
    @Override
    public void visit(Body n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getBodyExpr(), parent, arg, 1);
    }

    // Predicate, have children ParamDecl as parameteres and Body as body, param: 1, body: 2
    @Override
    public void visit(Predicate n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        callableList.add(n.getName());
        int parent = nodeMap.rget(n);
        for (ParamDecl v : n.getParamList()) {
            addIntoMapAndGraph(v, parent, arg, 1);
        }
        addIntoMapAndGraph(n.getBody(), parent, arg, 2);
    }

    // Function, have children ParamDecl as parameteres, ExprOrFormula as return, and Body as body, param: 1, return: 2, body: 3
    @Override
    public void visit(Function n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        callableList.add(n.getName());
        int parent = nodeMap.rget(n);
        for (ParamDecl v : n.getParamList()) {
            addIntoMapAndGraph(v, parent, arg, 1);
        }
        addIntoMapAndGraph(n.getReturnType(), parent, arg, 2);
        addIntoMapAndGraph(n.getBody(), parent, arg, 3);
    }

    // Fact, have children Body as body, body: 1
    @Override
    public void visit(Fact n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getBody(), parent, arg, 1);
    }

    // Assert, have children Body as body, body: 1
    @Override
    public void visit(Assertion n, A arg) {
        if (nodeMap.rget(n) == null) {
            makeRoot(n);
        }
        int parent = nodeMap.rget(n);
        addIntoMapAndGraph(n.getBody(), parent, arg, 1);
    }

    // Run
    @Override
    public void visit(Run n, A arg) {
    }

    // Check
    @Override
    public void visit(Check n, A arg) {
    }

    // Combine the leaf nodes so convert AST to ASG
    public void AST2ASG() {
        int numVertices = graph.getNumVertices();
    
        for (int i = 0; i < numVertices; i++) {
            Node n1 = nodeMap.get(i);
            if (n1 instanceof ExprOrFormula) {
                int j = i + 1;
                while (j < numVertices) {
                    Node n2 = nodeMap.get(j);
                    if (n2 instanceof ExprOrFormula) {
                        if (NodeUtil.sameLeaf(n1, n2) && n1.getParent() != n2.getParent()) {
                            graph.combineVertices(i, j);
                            for (int k = j; k < numVertices - 1; k++) {
                                nodeMap.put(k, nodeMap.get(k + 1));
                                nodeMap.remove(k + 1);
                            }
                            if (equivMap.containsKey(i)) {
                                equivMap.get(i).add(addlNodes.size());
                            } else {
                                List<Integer> equivList = new ArrayList<Integer>();
                                equivList.add(addlNodes.size());
                                equivMap.put(i, equivList);
                            }
                            addlNodes.add(n2);
                            numVertices--;
                            continue; // Continue from the adjusted value of j without incrementing it.
                        }
                    }
                    j++;
                }
            }
        }
    }

    // Only for the ExprOrFormula-centered graph (prior to AST2ASG)
    // TODO: Debug this method
    public void simplify() {
        int numVertices = graph.getNumVertices();
        List<Integer> verticesToRemove = new ArrayList<>();
        Map<Integer, Pair<Integer, Integer>> edgeUpdates = new HashMap<>();
    
        // Step 1: Identify vertices to remove
        for (int i = 0; i < numVertices; ++i) {
            Node n = nodeMap.get(i);
            if (n instanceof Body) {
                Body b = (Body) n;
                ExprOrFormula e = b.getBodyExpr();
                int bid = nodeMap.rget(b);
                int eid = nodeMap.rget(e);
                int pid = nodeMap.rget(b.getParent());
                int edgeWeight = graph.getEdgeWeight(bid, eid);
                verticesToRemove.add(bid);
                edgeUpdates.put(pid, Pair.of(eid, edgeWeight));  // Save the new edge to add later
            } else if (!(n instanceof ExprOrFormula)) {
                verticesToRemove.add(i);
                // recursively remove all children of n
                verticesToRemove.addAll(getDescendents(i));
            }
        }

        for (Map.Entry<Integer, Pair<Integer, Integer>> entry : edgeUpdates.entrySet()) {
            graph.addEdge(entry.getKey(), entry.getValue().a, entry.getValue().b);
        }

        // Step 2: Remove vertices
        for (int i : verticesToRemove) {
            System.out.println("Removing vertex " + i + " " + nodeMap.get(i) + " for simplification.");
            graph.removeVertex(i);
        }
    
        // Step 3: Update nodeMap
        for (int i = 0; i < numVertices; ++i) {
            if (verticesToRemove.contains(i)) {
                for (int j = i; j < numVertices - 1; ++j) {
                    nodeMap.put(j, nodeMap.get(j + 1));
                    nodeMap.remove(j + 1);
                }
                numVertices--;
            }
        }
    
    }

    private List<Integer> getDescendents(int i) {
        List<Integer> descendents = new ArrayList<>();
        List<Integer> directChildren = new ArrayList<>();
        for (int j = 0; j < graph.getNumVertices(); ++j) {
            if (graph.getEdgeWeight(i, j) > 0) {
                directChildren.add(j);
                descendents.add(j);
            }
        }
        for (int j : directChildren) {
            descendents.addAll(getDescendents(j));
        }
        return descendents;
    }

    public Map<Integer, List<Integer>> getEquivMap() {
        return equivMap;
    }

    public void saveNodeMap(String filepath, String label) {
        for (int i = 0; i < graph.getNumVertices(); ++i) {
            Node n = nodeMap.get(i);
            try {
                FileOutputStream fileOut = new FileOutputStream(filepath + "/" + label + "_" + i + ".alloynode");
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                objectOut.writeObject(n);
                objectOut.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        int i = 0;
        for (Node n : addlNodes) {
            try {
                FileOutputStream fileOut = new FileOutputStream(filepath + "/" + label + "_addl_" + i + ".alloynode");
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                objectOut.writeObject(n);
                objectOut.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            i++;
        }
    }
}
