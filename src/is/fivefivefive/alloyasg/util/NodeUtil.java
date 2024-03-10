package is.fivefivefive.alloyasg.util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.asg.ASGraph;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.etc.Triple;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Body;
import parser.ast.nodes.Node;
import parser.ast.nodes.OpenDecl;
import parser.ast.nodes.Assertion;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.Call;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.Check;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.ExprOrFormula;
import parser.ast.nodes.Fact;
import parser.ast.nodes.FieldDecl;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.Function;
import parser.ast.nodes.ITEExprOrFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
import parser.ast.nodes.ListExprOrFormula;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.ModuleDecl;
import parser.ast.nodes.ParamDecl;
import parser.ast.nodes.Predicate;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.Run;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.ast.nodes.SigDecl.MULT;
import parser.etc.Pair;

public class NodeUtil {
    // Check if a node is a leaf node
    public static boolean isLeaf(Node n) {
        return n.getChildren().isEmpty();
    }
    // Check if two leaf nodes are the same
    public static boolean sameLeaf(Node n1, Node n2) {
        if (!isLeaf(n1) || !isLeaf(n2)) {
            return false;
        }
        if (n1.getClass() != n2.getClass()) {
            return false;
        }
        if (n1 instanceof ConstExpr) {
            ConstExpr c1 = (ConstExpr)n1;
            ConstExpr c2 = (ConstExpr)n2;
            if (c1.getValue().equals(c2.getValue())) {
                return true;
            } else {
                return false;
            }
        }
        if (n1 instanceof SigExpr) {
            SigExpr s1 = (SigExpr)n1;
            SigExpr s2 = (SigExpr)n2;
            if (s1.getName().equals(s2.getName())) {
                return true;
            } else {
                return false;
            }
        }
        if (n1 instanceof FieldExpr) {
            FieldExpr f1 = (FieldExpr)n1;
            FieldExpr f2 = (FieldExpr)n2;
            if (f1.getName().equals(f2.getName())) {
                Node grandpa1 = f1.getParent().getParent();
                Node grandpa2 = f2.getParent().getParent();
                for (Node child1 : grandpa1.getChildren()) {
                    for (Node child2 : grandpa2.getChildren()) {
                        if (child1 instanceof ExprOrFormula && child2 instanceof ExprOrFormula) {
                            if (sameLeaf(child1, child2)) {
                                return true;
                            }
                        }
                    }
                }
            } else {
                return false;
            }
        }
        if (n1 instanceof VarExpr) {
            Node current = n1.getParent();
            while (current.getParent() != null) {
                current = current.getParent();
                for (Node child : current.getChildren()) {
                    if (child instanceof VarDecl) {
                        VarDecl v = (VarDecl)child;
                        if (v.getVariables().contains(n1)) {
                            if (v.getVariables().contains(n2)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }

    // Check if two nodes are equivalent in terms of their structure
    public static boolean equivNodes(Node n1, Node n2) {
        if (n1.getClass() != n2.getClass()) {
            return false;
        }
        if (n1 == n2) {
            return true;
        }
        if (n1 instanceof Body) {
            return true;
        }
        if (n1 instanceof UnaryExpr) {
            UnaryExpr u1 = (UnaryExpr)n1;
            UnaryExpr u2 = (UnaryExpr)n2;
            if (u1.getOp() == u2.getOp()) {
                return equivNodes(u1.getChildren().get(0), u2.getChildren().get(0));
            } else {
                return false;
            }
        }
        if (n1 instanceof UnaryFormula) {
            UnaryFormula u1 = (UnaryFormula)n1;
            UnaryFormula u2 = (UnaryFormula)n2;
            if (u1.getOp() == u2.getOp()) {
                return equivNodes(u1.getChildren().get(0), u2.getChildren().get(0));
            } else {
                return false;
            }
        }
        if (n1 instanceof BinaryExpr) {
            BinaryExpr b1 = (BinaryExpr)n1;
            BinaryExpr b2 = (BinaryExpr)n2;
            if (b1.getOp() == b2.getOp()) {
                return equivNodes(n1.getChildren().get(0), n2.getChildren().get(0))
                        && equivNodes(n1.getChildren().get(1), n2.getChildren().get(1));
            } else {
                return false;
            }
        }

        if (n1 instanceof BinaryFormula) {
            BinaryFormula b1 = (BinaryFormula)n1;
            BinaryFormula b2 = (BinaryFormula)n2;
            if (b1.getOp() == b2.getOp()) {
            return equivNodes(n1.getChildren().get(0), n2.getChildren().get(0))
                && equivNodes(n1.getChildren().get(1), n2.getChildren().get(1));
            } else {
                return false;
            }
        }

        if (n1 instanceof ListExprOrFormula) {
            ListExprOrFormula l1 = (ListExprOrFormula)n1;
            ListExprOrFormula l2 = (ListExprOrFormula)n2;
            for (ExprOrFormula e1 : l1.getArguments()) {
                boolean found = false;
                for (ExprOrFormula e2 : l2.getArguments()) {
                    if (equivNodes(e1, e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof CallExpr) {
            CallExpr c1 = (CallExpr)n1;
            CallExpr c2 = (CallExpr)n2;
            for (ExprOrFormula e1 : c1.getArguments()) {
                boolean found = false;
                for (ExprOrFormula e2 : c2.getArguments()) {
                    if (equivNodes(e1, e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof CallFormula) {
            CallFormula c1 = (CallFormula)n1;
            CallFormula c2 = (CallFormula)n2;
            for (ExprOrFormula e1 : c1.getArguments()) {
                boolean found = false;
                for (ExprOrFormula e2 : c2.getArguments()) {
                    if (equivNodes(e1, e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof QtExpr) {
            QtExpr q1 = (QtExpr)n1;
            QtExpr q2 = (QtExpr)n2;
            if (q1.getOp() == q2.getOp()) {
                for (VarDecl v1 : q1.getVarDecls()) {
                    boolean found = false;
                    for (VarDecl v2 : q2.getVarDecls()) {
                        if (equivNodes(v1, v2)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        }
        if (n1 instanceof QtFormula) {
            QtFormula q1 = (QtFormula)n1;
            QtFormula q2 = (QtFormula)n2;
            if (q1.getOp() == q2.getOp()) {
                for (VarDecl v1 : q1.getVarDecls()) {
                    boolean found = false;
                    for (VarDecl v2 : q2.getVarDecls()) {
                        if (equivNodes(v1, v2)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        }
        if (n1 instanceof SigDecl) {
            SigDecl s1 = (SigDecl)n1;
            SigDecl s2 = (SigDecl)n2;
            if (!equivNodes(s1.getParent(), s2.getParent())) {
                return false;
            }
            List<FieldDecl> f1s = s1.getFieldList();
            List<FieldDecl> f2s = s2.getFieldList();
            if (f1s.size() != f2s.size()) {
                return false;
            }
            for (FieldDecl f1 : f1s) {
                boolean found = false;
                for (FieldDecl f2 : f2s) {
                    if (equivNodes(f1, f2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof FieldDecl) {
            FieldDecl f1 = (FieldDecl)n1;
            FieldDecl f2 = (FieldDecl)n2;
            if (!equivNodes(f1.getParent(), f2.getParent())) {
                return false;
            }
            List<ExprOrFormula> v1s = f1.getVariables();
            List<ExprOrFormula> v2s = f2.getVariables();
            if (v1s.size() != v2s.size()) {
                return false;
            }
            for (ExprOrFormula v1 : v1s) {
                boolean found = false;
                for (ExprOrFormula v2 : v2s) {
                    if (equivNodes(v1, v2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            if (!equivNodes(f1.getExpr(), f2.getExpr())) {
                return false;
            }
            return true;
        }
        if (n1 instanceof ParamDecl) {
            ParamDecl p1 = (ParamDecl)n1;
            ParamDecl p2 = (ParamDecl)n2;
            List<ExprOrFormula> v1s = p1.getVariables();
            List<ExprOrFormula> v2s = p2.getVariables();
            if (v1s.size() != v2s.size()) {
                return false;
            }
            for (ExprOrFormula v1 : v1s) {
                boolean found = false;
                for (ExprOrFormula v2 : v2s) {
                    if (equivNodes(v1, v2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            if (!equivNodes(p1.getExpr(), p2.getExpr())) {
                return false;
            }
            return true;
        }
        if (n1 instanceof VarDecl) {
            VarDecl v1 = (VarDecl)n1;
            VarDecl v2 = (VarDecl)n2;
            List<ExprOrFormula> v1s = v1.getVariables();
            List<ExprOrFormula> v2s = v2.getVariables();
            if (v1s.size() != v2s.size()) {
                return false;
            }
            for (ExprOrFormula v1e : v1s) {
                boolean found = false;
                for (ExprOrFormula v2e : v2s) {
                    if (equivNodes(v1e, v2e)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            if (!equivNodes(v1.getExpr(), v2.getExpr())) {
                return false;
            }
            return true;
        }
        if (n1 instanceof Call) {
            Call c1 = (Call)n1;
            Call c2 = (Call)n2;
            if (c1.getArguments().size() != c2.getArguments().size()) {
                return false;
            }
            for (ExprOrFormula e1 : c1.getArguments()) {
                boolean found = false;
                for (ExprOrFormula e2 : c2.getArguments()) {
                    if (equivNodes(e1, e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof QtExpr) {
            QtExpr q1 = (QtExpr) n1;
            QtExpr q2 = (QtExpr) n2;
            if (q1.getVarDecls().size() != q2.getVarDecls().size()) {
                return false;
            }
            if (q1.getOp() != q2.getOp()) {
                return false;
            }
            for (VarDecl v1 : q1.getVarDecls()) {
                boolean found = false;
                for (VarDecl v2 : q2.getVarDecls()) {
                    if (equivNodes(v1, v2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof QtFormula) {
            QtFormula q1 = (QtFormula) n1;
            QtFormula q2 = (QtFormula) n2;
            if (q1.getVarDecls().size() != q2.getVarDecls().size()) {
                return false;
            }
            if (q1.getOp() != q2.getOp()) {
                return false;
            }
            for (VarDecl v1 : q1.getVarDecls()) {
                boolean found = false;
                for (VarDecl v2 : q2.getVarDecls()) {
                    if (equivNodes(v1, v2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
        if (n1 instanceof ITEExprOrFormula) {
            ITEExprOrFormula i1 = (ITEExprOrFormula)n1;
            ITEExprOrFormula i2 = (ITEExprOrFormula)n2;
            if (!equivNodes(i1.getCondition(), i2.getCondition())) {
                return false;
            }
            if (!equivNodes(i1.getThenClause(), i2.getThenClause())) {
                return false;
            }
            if (!equivNodes(i1.getElseClause(), i2.getElseClause())) {
                return false;
            }
            return true;
        }
        if (n1 instanceof LetExpr) {
            LetExpr l1 = (LetExpr)n1;
            LetExpr l2 = (LetExpr)n2;
            if (!equivNodes(l1.getVar(), l2.getVar())) {
                return false;
            }
            if (!equivNodes(l1.getBound(), l2.getBound())) {
                return false;
            }
            return true;
        }

        if (n1 instanceof ExprOrFormula) {
            if (sameLeaf(n1, n2)) {
                return true;
            } else {
                return false;
            }
        }

        if (n1 instanceof Predicate) {
            if (n1.getParent() != n2.getParent()) {
                return false;
            }
            Predicate p1 = (Predicate)n1;
            Predicate p2 = (Predicate)n2;
            if (p1.getParamList().size() != p2.getParamList().size()) {
                return false;
            }
            for (ParamDecl e1 : p1.getParamList()) {
                boolean found = false;
                for (ParamDecl e2 : p2.getParamList()) {
                    if (equivNodes(e1, e2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Triple<ASGraph, ASGraph, DoubleMap<Integer, Node>> compareASG(ASGraph g1, ASGraph g2, DoubleMap<Integer, Node> m1, DoubleMap<Integer, Node> m2) {
        DoubleMap<Integer, Integer> equivMap = new DoubleMap<Integer, Integer>();
        int size1 = g1.getNumVertices();
        int size2 = g2.getNumVertices();
        for (int i = 0; i < size1; ++i) {
            Node n1 = m1.get(i);
            for (int j = 0; j < size2; ++j) {
                Node n2 = m2.get(j);
                if (equivNodes(n1, n2) && !equivMap.containsKey(i) && !equivMap.containsValue(j)) {
                    equivMap.put(i, j);
                    break; // fcfs
                }
            }
        }
        int newSize = size1 + size2 - equivMap.size();
        ASGraph g1p = new ASGraph(newSize);
        ASGraph g2p = new ASGraph(newSize);
        DoubleMap<Integer, Node> newMap = new DoubleMap<Integer, Node>();
        // copy g1
        for (int i = 0; i < size1; ++i) {
            Node n1 = m1.get(i);
            newMap.put(i, n1);
            for (int j = 0; j < size1; ++j) {
                int weight = g1.getEdgeWeight(i, j);
                if (weight > 0) {
                    g1p.addEdge(i, j, weight);
                }
            }
        }
        // make g2 with the map
        int offset = size1;
        List<Triple<Integer, Integer, Integer>> uncatchedEdges = new ArrayList<Triple<Integer, Integer, Integer>>();
        Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < size2; ++i) {
            Node n2 = m2.get(i);
            if (!equivMap.containsValue(i)) {
                newMap.put(offset, n2);
                indexMap.put(i, offset);
                for (int j = 0; j < size2; ++j) {
                    int weight = g2.getEdgeWeight(i, j);
                    if (equivMap.containsValue(j) && weight > 0) {
                        g2p.addEdge(offset, equivMap.rget(j), weight);
                    } else if (weight > 0) {
                        uncatchedEdges.add(new Triple<Integer, Integer, Integer>(i, j, weight));
                    }
                }
                offset++;
            } else {
                indexMap.put(i, equivMap.rget(i));
                for (int j = 0; j < size2; ++j) {
                    int weight = g2.getEdgeWeight(i, j);
                    if (equivMap.containsValue(j) && weight > 0) {
                        g2p.addEdge(equivMap.rget(i), equivMap.rget(j), weight);
                    } else if (weight > 0) {
                        uncatchedEdges.add(new Triple<Integer, Integer, Integer>(i, j, weight));
                    }
                }
            }
        }
        // add uncatched edges to g2p
        for (Triple<Integer, Integer, Integer> edge : uncatchedEdges) {
            int n1 = edge.x;
            int n2 = edge.y;
            int weight = edge.z;
            int i = indexMap.get(n1);
            int j = indexMap.get(n2);
            g2p.addEdge(i, j, weight);
        }
        return new Triple<ASGraph, ASGraph, DoubleMap<Integer, Node>>(g1p, g2p, newMap);
    }

    public static Triple<ASGraph, ASGraph, DoubleMap<Integer, Node>> compareASG(ASGVisitor<Object> asg1, ASGVisitor<Object> asg2) {
        ASGraph g1 = asg1.getGraph();
        ASGraph g2 = asg2.getGraph();
        DoubleMap<Integer, Node> m1 = asg1.getNodeMap();
        DoubleMap<Integer, Node> m2 = asg2.getNodeMap();
        return compareASG(g1, g2, m1, m2);
    }

    // A function to vectorize a node
    // Significance of the vector elements decrease with the heuristic of the index.
    // First element gives the class
    // Last element gives the index of the node in the nodeMap (redacted)
    // This is the first, context-free representation of a node
    // we must learn the vector representation of each node then
    public static Pair<List<Integer>, String> initialVectorize(Node n, DoubleMap<Integer, Node> nodeMap) {
        List<Integer> vec = new ArrayList<Integer>(8);
        String nameBuffer = "";
        for (int i = 0; i < 8; ++i) {
            vec.add(0);
        }
        // vec.set(7, nodeMap.rget(n));
        if (n instanceof ModelUnit) {
            vec.set(0, 0);
            return Pair.of(vec, "<ROOT>");
        }
        if (n instanceof ModuleDecl) {
            vec.set(0, 1);
            return Pair.of(vec, ((ModuleDecl)n).getModelName());
        }
        if (n instanceof OpenDecl) {
            vec.set(0, 2);
            return Pair.of(vec, ((OpenDecl)n).getFileName());
        }
        if (n instanceof SigDecl) {
            vec.set(0, 3);
            SigDecl s = (SigDecl)n;
            vec.set(2, s.isAbstract() ? 1 : 0);
            MULT mul = s.getMult();
            switch(mul) {
                case LONE:
                    vec.set(3, 1);
                    break;
                case ONE:
                    vec.set(3, 2);
                    break;
                case SOME:
                    vec.set(3, 3);
                    break;
                default:
                    vec.set(3, 0);
                    break;
            }
            vec.set(4, s.isTopLevel() ? 1 : 0);
            vec.set(5, s.isSubsig() ? 1 : 0);
            String name = s.getName();
            return Pair.of(vec, name);
            // vec.set(5, s.hasSigFact() ? 1 : 0);
        }
        // Reldecl , relations
        if (n instanceof RelDecl) {
            vec.set(0, 4);
            RelDecl r = (RelDecl)n;
            vec.set(2, r.isDisjoint() ? 1 : 0);
            vec.set(3, r.isVariable() ? 1 : 0);
            for (String name : r.getNames()) {
                nameBuffer += name + " ";
            }
        }
        // FieldDecl, fields, subset of RelDecl
        if (n instanceof FieldDecl) {
            vec.set(1, 1);
            FieldDecl f = (FieldDecl)n;
            for (ExprOrFormula e : f.getVariables()) {
                if (e instanceof SigExpr) {
                    vec.set(6, 1);
                }
            }
            return Pair.of(vec, nameBuffer);
        }
        // ParamDecl, parameters, subset of RelDecl
        if (n instanceof ParamDecl) {
            vec.set(1, 2);
            return Pair.of(vec, "PARAMETER");
        }
        // VarDecl, variables, subset of RelDecl
        if (n instanceof VarDecl) {
            vec.set(1, 3);
            return Pair.of(vec, nameBuffer);
        }
        // ExprOrFormula, big set
        if (n instanceof ExprOrFormula) {
            vec.set(0, 5);
        }
        // SigExpr, subset of ExprOrFormula, 
        if (n instanceof SigExpr) {
            vec.set(1, 4);
            return Pair.of(vec, ((SigExpr)n).getName());
        }
        // VarExpr, subset of ExprOrFormula
        if (n instanceof VarExpr) {
            vec.set(1, 5);
            return Pair.of(vec, ((VarExpr)n).getName());
        }
        // FieldExpr, subset of ExprOrFormula
        if (n instanceof FieldExpr) {
            vec.set(1, 6);
            return Pair.of(vec, ((FieldExpr)n).getName());
        }
        // CallExpr, subset of ExprOrFormula
        if (n instanceof CallExpr) {
            vec.set(1, 7);
            return Pair.of(vec, ((CallExpr)n).getName());
        }
        // CallFormula, subset of ExprOrFormula
        if (n instanceof CallFormula) {
            vec.set(1, 8);
            return Pair.of(vec, ((CallFormula)n).getName());
        }
        // BinaryExpr, subset of ExprOrFormula
        if (n instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr)n;
            vec.set(1, 9);
            switch (b.getOp()) {
                case ARROW:
                    vec.set(2, 1);
                    break;
                case ANY_ARROW_SOME:
                    vec.set(2, 2);
                    break;
                case ANY_ARROW_ONE:
                    vec.set(2, 3);
                    break;
                case ANY_ARROW_LONE:
                    vec.set(2, 4);
                    break;
                case SOME_ARROW_ANY:
                    vec.set(2, 5);
                    break;
                case SOME_ARROW_SOME:
                    vec.set(2, 6);
                    break;
                case SOME_ARROW_ONE:
                    vec.set(2, 7);
                    break;
                case SOME_ARROW_LONE:
                    vec.set(2, 8);
                    break;
                case ONE_ARROW_ANY:
                    vec.set(2, 9);
                    break;
                case ONE_ARROW_SOME:
                    vec.set(2, 10);
                    break;
                case ONE_ARROW_ONE:
                    vec.set(2, 11);
                    break;
                case ONE_ARROW_LONE:
                    vec.set(2, 12);
                    break;
                case LONE_ARROW_ANY:
                    vec.set(2, 13);
                    break;
                case LONE_ARROW_SOME:
                    vec.set(2, 14);
                    break;
                case LONE_ARROW_ONE:
                    vec.set(2, 15);
                    break;
                case LONE_ARROW_LONE:
                    vec.set(2, 16);
                    break;
                case ISSEQ_ARROW_LONE:
                    vec.set(2, 17);
                    break;
                case JOIN:
                    vec.set(2, 18);
                    break;
                case DOMAIN:
                    vec.set(2, 19);
                    break;
                case RANGE:
                    vec.set(2, 20);
                    break;
                case INTERSECT:
                    vec.set(2, 21);
                    break;
                case PLUS:
                    vec.set(2, 22);
                    break;
                case MINUS:
                    vec.set(2, 23);
                    break;
                case MUL:
                    vec.set(2, 24);
                    break;
                case DIV:
                    vec.set(2, 25);
                    break;
                case REM:
                    vec.set(2, 26);
                    break;
                case SHL:
                    vec.set(2, 27);
                    break;
                case SHA:
                    vec.set(2, 28);
                    break;
                case SHR:
                    vec.set(2, 29);
                    break;
                default:
                    vec.set(2, 0);
                    break;
            }
            return Pair.of(vec, b.getOp().toString());
        }
        // BinaryFormula, subset of ExprOrFormula
        if (n instanceof BinaryFormula) {
            BinaryFormula b = (BinaryFormula)n;
            vec.set(1, 10);
            switch (b.getOp()) {
                case AND:
                    vec.set(2, 1);
                    break;
                case OR:
                    vec.set(2, 2);
                    break;
                case IMPLIES:
                    vec.set(2, 3);
                    break;
                case IFF:
                    vec.set(2, 4);
                    break;
                case IN:
                    vec.set(2, 5);
                    break;
                case NOT_IN:
                    vec.set(2, 6);
                    break;
                case EQUALS:
                    vec.set(2, 7);
                    break;
                case NOT_EQUALS:
                    vec.set(2, 8);
                    break;
                case LT:
                    vec.set(2, 9);
                    break;
                case LTE:
                    vec.set(2, 10);
                    break;
                case GT:
                    vec.set(2, 11);
                    break;
                case GTE:
                    vec.set(2, 12);
                    break;
                default:
                    vec.set(2, 0);
                    break;
            }
            return Pair.of(vec, b.getOp().toString());
        }
        // ListExprOrFormula, subset of ExprOrFormula
        if (n instanceof ListExprOrFormula) {
            vec.set(1, 11);
            vec.set(3, ((ListExprOrFormula)n).getArguments().size());
        }
        // ListExpr, subset of ListExprOrFormula
        if (n instanceof ListExpr) {
            ListExpr l = (ListExpr)n;
            vec.set(2, l.getOp() == ListExpr.ListOp.DISJOINT ? 1 : 2);
            return Pair.of(vec, l.getOp().toString());
        }
        // ListFormula, subset of ListExprOrFormula
        if (n instanceof ListFormula) {
            ListFormula l = (ListFormula)n;
            vec.set(2, l.getOp() == ListFormula.ListOp.AND ? 3 : 4);
            return Pair.of(vec, l.getOp().toString());
        }
        // ITEExprOrFormula, subset of ExprOrFormula
        if (n instanceof ITEExprOrFormula) {
            vec.set(1, 12);
            return Pair.of(vec, "ITE");
        }
        // LetExpr, subset of ExprOrFormula
        if (n instanceof LetExpr) {
            vec.set(1, 13);
            return Pair.of(vec, "LET");
        }
        // Call, subset of ExprOrFormula
        if (n instanceof Call) {
            vec.set(1, 14);
            return Pair.of(vec, ((Call)n).getName());
        }
        // QtExpr, subset of QtExprOrFormula
        if (n instanceof QtExpr) {
            vec.set(1, 15);
            vec.set(2, ((QtExpr)n).getOp() == QtExpr.Quantifier.SUM ? 1 : 2);
            return Pair.of(vec, ((QtExpr)n).toString());
        }
        // QtFormula, subset of QtExprOrFormula
        if (n instanceof QtFormula) {
            vec.set(1, 16);
            QtFormula q = (QtFormula)n;
            switch (q.getOp()) {
                case ALL:
                    vec.set(2, 1);
                    break;
                case NO:
                    vec.set(2, 2);
                    break;
                case LONE:
                    vec.set(2, 3);
                    break;
                case ONE:
                    vec.set(2, 4);
                    break;
                case SOME:
                    vec.set(2, 5);
                    break;
                default:
                    vec.set(2, 0);
                    break;
            }
            return Pair.of(vec, ((QtFormula)n).toString());
        }
        // UnaryExpr, subset of UnaryExprOrFormula
        if (n instanceof UnaryExpr) {
            vec.set(1, 17);
            UnaryExpr u = (UnaryExpr)n;
            switch (u.getOp()) {
                case SET:
                    vec.set(2, 1);
                    break;
                case LONE:
                    vec.set(2, 2);
                    break;
                case ONE:
                    vec.set(2, 3);
                    break;
                case SOME:
                    vec.set(2, 4);
                    break;
                case EXACTLYOF:
                    vec.set(2, 5);
                    break;
                case TRANSPOSE:
                    vec.set(2, 6);
                    break;
                case RCLOSURE: 
                    vec.set(2, 7);
                    break;
                case CLOSURE:
                    vec.set(2, 8);
                    break;
                case CARDINALITY:
                    vec.set(2, 9);
                    break;
                case CAST2INT:
                    vec.set(2, 10);
                    break;
                case CAST2SIGINT:
                    vec.set(2, 11);
                    break;
                case NOOP:
                    vec.set(2, 12);
                    break;
                case PRIME:
                    vec.set(2, 13);
                    break;
            }
            return Pair.of(vec, u.getOp().toString());
        }
        if (n instanceof UnaryFormula) {
            vec.set(1, 18);
            UnaryFormula u = (UnaryFormula)n;
            switch (u.getOp()) {
                case LONE:
                    vec.set(2, 1);
                    break;
                case ONE:
                    vec.set(2, 2);
                    break;
                case SOME:
                    vec.set(2, 3);
                    break;
                case NOT:
                    vec.set(2, 4);
                    break;
                case NO:
                    vec.set(2, 5);
                    break;
                case BEFORE:
                    vec.set(2, 6);
                    break;
                case HISTORICALLY:
                    vec.set(2, 7);
                    break;
                case ONCE:
                    vec.set(2, 8);
                    break;
                case EVENTUALLY:
                    vec.set(2, 9);
                    break;
                case ALWAYS:
                    vec.set(2, 10);
                    break;
                case AFTER:
                    vec.set(2, 11);
                    break;
            }
            return Pair.of(vec, u.getOp().toString());
        }
        if (n instanceof ConstExpr) {
            vec.set(1, 19);
            ConstExpr c = (ConstExpr)n;
            switch (c.getValue()) {
                case "IDEN":
                case "iden":
                    vec.set(2, 1);
                    break;
                case "EMPTYNESS":
                case "none":
                case "empty":
                    vec.set(2, 1);
                    vec.set(3, 1);
                    break;
                case "STRING":
                    vec.set(2, 2);
                    break;
                case "TRUE":
                case "true":
                    vec.set(2, 3);
                    vec.set(3, 1);
                    break;
                case "FALSE":
                case "false":
                    vec.set(2, 3);
                    vec.set(3, 2);
                    break;
                default:
                    vec.set(2, 0);
                    int value;
                    try {
                        value = Integer.parseInt(c.getValue());
                    }
                    catch (NumberFormatException e) {
                        value = 0;
                    }
                    vec.set(3, value);
            }
            return Pair.of(vec, c.getValue());
        }
        // Body
        if (n instanceof Body) {
            vec.set(0, 6);
            return Pair.of(vec, "BODY");
        }
        // Predicate
        if (n instanceof Predicate) {
            vec.set(0, 7);
            return Pair.of(vec, ((Predicate)n).getName());
        }
        // Assert
        if (n instanceof Assertion) {
            vec.set(0, 8);
            return Pair.of(vec, "ASSERTION");
        }
        // Function
        if (n instanceof Function) {
            vec.set(0, 9);
            return Pair.of(vec, ((Function)n).getName());
        }
        // Fact
        if (n instanceof Fact) {
            vec.set(0, 10);
            return Pair.of(vec, "FACT");
        }
        // Run
        if (n instanceof Run) {
            vec.set(0, 11);
            return Pair.of(vec, "RUN");
        }
        // Check
        if (n instanceof Check) {
            vec.set(0, 12);
            return Pair.of(vec, "CHECK");
        }
        System.out.println("UNKNOWN NODE: " + n.getClass().getName());
        return Pair.of(vec, "UNKNOWN NODE");
    }
    public static List<Double> weight(List<Integer> prior, List<Double> weights) {
        List<Double> vec = new ArrayList<Double>();
        for (int i = 0; i < prior.size(); ++i) {
            vec.add(prior.get(i) * weights.get(i));
        }
        return vec;
    }

    public static boolean isomorphicRelDecl(RelDecl v1, RelDecl v2) {
        return (v1.getVariables().size() == v2.getVariables().size()) && equivNodes(v1.getExpr(), v2.getExpr());
    }

    public static List<Double> WEIGHT = new ArrayList<Double>() {{
        add(0.5); // class
        add(0.25);
        add(0.125);
        add(0.0625);
        add(0.03125);
        add(0.015625);
        add(0.0078125);
        add(0.0078125); // index
    }};
}