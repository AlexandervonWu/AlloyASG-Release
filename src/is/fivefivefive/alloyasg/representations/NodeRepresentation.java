package is.fivefivefive.alloyasg.representations;

import java.util.List;
import java.util.Map;
import com.abdulfatir.jcomplexnumber.ComplexNumber;
import is.fivefivefive.alloyasg.asg.ASGVisitor;
import is.fivefivefive.alloyasg.etc.DoubleMap;
import is.fivefivefive.alloyasg.exceptions.ScopeNotFoundException;
import is.fivefivefive.alloyasg.exceptions.UnsupportedConstantException;
import parser.ast.nodes.BinaryExpr;
import parser.ast.nodes.BinaryFormula;
import parser.ast.nodes.Paragraph;
import parser.ast.nodes.QtExpr;
import parser.ast.nodes.QtFormula;
import parser.ast.nodes.RelDecl;
import parser.ast.nodes.SigDecl;
import parser.ast.nodes.SigExpr;
import parser.ast.nodes.UnaryExpr;
import parser.ast.nodes.UnaryFormula;
import parser.ast.nodes.VarDecl;
import parser.ast.nodes.VarExpr;
import parser.etc.Pair;
import parser.ast.nodes.CallExpr;
import parser.ast.nodes.CallFormula;
import parser.ast.nodes.ConstExpr;
import parser.ast.nodes.FieldExpr;
import parser.ast.nodes.ITEExpr;
import parser.ast.nodes.ITEFormula;
import parser.ast.nodes.LetExpr;
import parser.ast.nodes.ListExpr;
import parser.ast.nodes.ListFormula;
import parser.ast.nodes.ModelUnit;
import parser.ast.nodes.Node;



public class NodeRepresentation {
    private double signature; // syntactic signature, -pi ~ pi
    private double semantic; // semantic vector (list of integers) - either simple integer or finitely enumerable
    public static final double UNIT = Math.PI / 13;
    public static final double EPSILON = UNIT / 128;
    public static final double EPSILON_SMALL = UNIT / 65536;
    private boolean flagExprOrFormula = true;

    public NodeRepresentation() {
        signature = 0;
        semantic = 1;
    }

    public NodeRepresentation(Paragraph p) {
        // Paragraphes are roots of the subtrees of the AS-Forest
        // signature == 0
        signature = 0;
        semantic = 1;
    }

    public NodeRepresentation(double signature, double semantic) {
        this.signature = signature;
        this.semantic = semantic;
    }

    public double getSignature() {
        return signature;
    }

    public double getSemanticRepresentation() {
        return semantic;
    }

    public int getSyntacticRepresentation() {
        return (int) Math.round(signature / UNIT);
    }

    public ComplexNumber getComplexRepresentation() {
        return new ComplexNumber(signature * Math.cos(semantic), signature * Math.sin(semantic));
    }

    public NodeRepresentation(ASGVisitor<Object> asgv, Node n) 
            throws ScopeNotFoundException, UnsupportedConstantException {
        // Expressions and Formulas should have their own signatures
        // Consider closed cycles;
        DoubleMap<Integer, Node> nodeMap = asgv.getNodeMap();
        Map<Integer, Map<String, Integer>> scopeRelMap = asgv.getScopeRelMap();

        if (n instanceof SigExpr) {
            signature = UNIT;
            semantic = getSigSemantic((SigExpr) n, asgv);
        }
        if (n instanceof VarExpr) {
            signature = 2 * UNIT;
            String name = ((VarExpr) n).getName();
            semantic = getScope(n, name, nodeMap, scopeRelMap).b.get(name);
        }
        if (n instanceof RelDecl) {
            flagExprOrFormula = false;
            signature = -2 * UNIT;
            List<String> names = ((RelDecl) n).getNames();
            Pair<Integer, Map<String, Integer>> scope = getScope(n, names.get(0), nodeMap, scopeRelMap);
            int varCount = asgv.numRels();
            int exponent = 0;
            for (String name : names) {
                int varId = scope.b.get(name);
                semantic += varId * Math.pow(varCount, exponent);
                exponent++;
            }
        }
        if (n instanceof FieldExpr) {
            signature = 3 * UNIT;
            semantic = getFieldSemantic((FieldExpr) n, asgv);
            
        }
        if (n instanceof ConstExpr) {
            signature = 4 * UNIT;
            ConstExpr ce = (ConstExpr) n;
            try {
                semantic = Integer.parseInt(ce.getValue());
            } catch (Exception e) {
                // consider the non-integer constants with their own signatures
                if (ce.isBoolean()) {
                    if (ce.getValue().equals("true")) {
                        signature = 5 * UNIT;
                        semantic = 1;
                    } else {
                        semantic = 0;
                    }
                } else if (ce.getValue().equals("iden")) {
                    semantic = 10;
                } else {
                    // currently we don't support non-integer, non-boolean constants
                    throw new UnsupportedConstantException(ce.getValue());
                }
            }
        }
        if (n instanceof CallExpr) {
            signature = 6 * UNIT;
            semantic = asgv.getCallableList().indexOf(((CallExpr) n).getName());
        }
        if (n instanceof CallFormula) {
            signature = -6 * UNIT;
            semantic = asgv.getCallableList().indexOf(((CallFormula) n).getName());
        }
        if (n instanceof UnaryExpr) {
            signature = 7 * UNIT;
            UnaryExpr ue = (UnaryExpr) n;
            switch (ue.getOp()) {
                case SET:
                    semantic = 1;
                    break;
                case LONE:
                    semantic = 2;
                    break;
                case ONE:
                    semantic = 3;
                    break;
                case SOME:
                    semantic = 4;
                    break;
                case EXACTLYOF:
                    semantic = 5;
                    break;
                case TRANSPOSE:
                    semantic = 6;
                    break;
                case RCLOSURE:
                    semantic = 7;
                    break;
                case CLOSURE:
                    semantic = 8;
                    break;
                case CARDINALITY:
                    semantic = 9;
                    break;
                case CAST2INT:
                    semantic = 10;
                    break;
                case CAST2SIGINT:
                    semantic = 11;
                    break;
                case NOOP:
                    semantic = 0;
                    break;
                case PRIME:
                    semantic = 12;
                    break;
            }
        }
        if (n instanceof UnaryFormula) {
            signature = -7 * UNIT;
            UnaryFormula ue = (UnaryFormula) n;
            switch (ue.getOp()) {
                case LONE:
                    semantic = 1;
                    break;
                case ONE:
                    semantic = 2;
                    break;
                case SOME:
                    semantic = 3;
                    break;
                case NO:
                    semantic = 4;
                    break;
                case NOT:
                    semantic = 5;
                    break;
                case BEFORE:
                    semantic = 6;
                    break;
                case HISTORICALLY:
                    semantic = 7;
                    break;
                case ONCE:
                    semantic = 8;
                    break;
                case ALWAYS:
                    semantic = 9;
                    break;
                case EVENTUALLY:
                    semantic = 10;
                    break;
                case AFTER:
                    semantic = 11;
                    break;
            }
        }
        if (n instanceof BinaryExpr) {
            signature = 8 * UNIT;
            BinaryExpr be = (BinaryExpr) n;
            switch(be.getOp()) {
                case ARROW:
                    semantic = 1;
                    break;
                case ANY_ARROW_SOME:
                    semantic = 2;
                    break;
                case ANY_ARROW_ONE:
                    semantic = 3;
                    break;
                case ANY_ARROW_LONE:
                    semantic = 4;
                    break;
                case SOME_ARROW_ANY:
                    semantic = 5;
                    break;
                case SOME_ARROW_SOME:
                    semantic = 6;
                    break;
                case SOME_ARROW_ONE:
                    semantic = 7;
                    break;
                case SOME_ARROW_LONE:
                    semantic = 8;
                    break;
                case ONE_ARROW_ANY:
                    semantic = 9;
                    break;
                case ONE_ARROW_SOME:
                    semantic = 10;
                    break;
                case ONE_ARROW_ONE:
                    semantic = 11;
                    break;
                case ONE_ARROW_LONE:
                    semantic = 12;
                    break;
                case LONE_ARROW_ANY:
                    semantic = 13;
                    break;
                case LONE_ARROW_SOME:
                    semantic = 14;
                    break;
                case LONE_ARROW_ONE:
                    semantic = 15;
                    break;
                case LONE_ARROW_LONE:
                    semantic = 16;
                    break;
                case ISSEQ_ARROW_LONE:
                    semantic = 17;
                    break;
                case JOIN:
                    semantic = 18;
                    break;
                case DOMAIN:
                    semantic = 19;
                    break;
                case RANGE:
                    semantic = 20;
                    break;
                case INTERSECT:
                    semantic = 21;
                    break;
                case PLUSPLUS:
                    semantic = 22;
                    break;
                case PLUS:
                    semantic = 23;
                    break;
                case IPLUS:
                    semantic = 24;
                    break;
                case MINUS:
                    semantic = 25;
                    break;
                case IMINUS:
                    semantic = 26;
                    break;
                case MUL:
                    semantic = 27;
                    break;
                case DIV:
                    semantic = 28;
                    break;
                case REM:
                    semantic = 29;
                    break;
                case SHL:
                    semantic = 30;
                    break;
                case SHA:
                    semantic = 31;
                    break;
                case SHR:
                    semantic = 32;
                    break;
            }
        }
        if (n instanceof BinaryFormula) {
            signature = -8 * UNIT;
            BinaryFormula bf = (BinaryFormula) n;
            switch (bf.getOp()) {
                case EQUALS:
                    semantic = 1;
                    break;
                case NOT_EQUALS:
                    semantic = 2;
                    break;
                case IMPLIES:
                    semantic = 3;
                    break;
                case LT:
                    semantic = 4;
                    break;
                case LTE:
                    semantic = 5;
                    break;
                case GT:
                    semantic = 6;
                    break;
                case GTE:
                    semantic = 7;
                    break;
                case NOT_LT:
                    semantic = 8;
                    break;
                case NOT_LTE:
                    semantic = 9;
                    break;
                case NOT_GT:
                    semantic = 10;
                    break;
                case NOT_GTE:
                    semantic = 11;
                    break;
                case IN:
                    semantic = 12;
                    break;
                case NOT_IN:
                    semantic = 13;
                    break;
                case AND:
                    semantic = 14;
                    break;
                case OR:
                    semantic = 15;
                    break;
                case IFF:
                    semantic = 16;
                    break;
                case UNTIL:
                    semantic = 17;
                    break;
                case RELEASES:
                    semantic = 18;
                    break;
                case SINCE:
                    semantic = 19;
                    break;
                case TRIGGERED:
                    semantic = 20;
                    break;
            }
        }
        if (n instanceof ListExpr) {
            signature = 9 * UNIT;
            ListExpr le = (ListExpr) n;
            if (le.getOp() == ListExpr.ListOp.DISJOINT) {
                semantic = 1;
            } else if (le.getOp() == ListExpr.ListOp.TOTALORDER) {
                semantic = 2;
            }
        }
        if (n instanceof ListFormula) {
            signature = -9 * UNIT;
            ListFormula lf = (ListFormula) n;
            if (lf.getOp() == ListFormula.ListOp.AND) {
                semantic = 1;
            } else if (lf.getOp() == ListFormula.ListOp.OR) {
                semantic = 2;
            }
        }
        if (n instanceof LetExpr) {
            signature = 10 * UNIT;
            semantic = 1;
        }
        if (n instanceof QtExpr) {
            signature = 11 * UNIT;
            QtExpr qte = (QtExpr) n;
            switch (qte.getOp()) {
                case SUM:
                    semantic = 1;
                    break;
                case COMPREHENSION:
                    semantic = 2;
                    break;
            }
        }
        if (n instanceof QtFormula) {
            signature = -11 * UNIT;
            QtFormula qtf = (QtFormula) n;
            switch (qtf.getOp()) {
                case ALL:
                    semantic = 1;
                    break;
                case NO:
                    semantic = 2;
                    break;
                case LONE:
                    semantic = 3;
                    break;
                case ONE:
                    semantic = 4;
                    break;
                case SOME:
                    semantic = 5;
                    break;
            }
        }
        if (n instanceof ITEExpr) {
            signature = 12 * UNIT;
            semantic = 1;
        }
        if (n instanceof ITEFormula) {
            signature = -12 * UNIT;
            semantic = 1;
        }
    }

    public double fineGrainSignature() {
        if (flagExprOrFormula) {
            return signature + (semantic * EPSILON);
        } else {
            return signature + (semantic * EPSILON_SMALL);
        }
    }

    public ComplexNumber toComplex() {
        return new ComplexNumber(semantic * Math.cos(signature), semantic * Math.sin(signature));
    }

    private Pair<Integer, Map<String, Integer>> getScope(Node n, String name, 
                DoubleMap<Integer, Node> nodeMap, 
                Map<Integer, Map<String, Integer>> scopeRelMap) throws ScopeNotFoundException {
        Node par = n.getParent();
        int parId = -1;
        try {
            parId = nodeMap.rget(par);
        } catch (Exception e){
            System.out.println("Error: cannot find scope for " + name + ", cannot process;");
            throw new ScopeNotFoundException(name, n);
        }
        Map<String, Integer> scope = null;
        while (par != null) {
            parId = nodeMap.rget(par);
            if (scopeRelMap.containsKey(parId)) {
                if (scopeRelMap.get(parId).containsKey(name)) {
                    scope = scopeRelMap.get(parId);
                    break;
                }
            }
            par = par.getParent();
        }
        if (scope == null) {
            System.out.println("Error: cannot find scope for " + name + ", cannot process;");
            throw new ScopeNotFoundException(name, n);
        }
        return Pair.of(parId, scope);
    }

    private int getSigSemantic(SigExpr se, ASGVisitor<Object> asgv) throws ScopeNotFoundException {
        try {
            return asgv.getSigList().indexOf(((SigExpr) se).getName());
        } catch (Exception e) {
            System.out.println("Error: cannot find semantic for " + se.getName() + ", cannot process;");
            throw new ScopeNotFoundException(se.getName(), se);
        }
    }

    private int getFieldSemantic(FieldExpr fe, ASGVisitor<Object> asgv) throws ScopeNotFoundException, UnsupportedConstantException {
        Node par = fe.getParent();
        String fieldName = fe.getName();
        boolean firstNoNOOP = true;
        while (!(par instanceof ModelUnit)) {
            if (par instanceof UnaryExpr && firstNoNOOP) {
                UnaryExpr ueof = (UnaryExpr) par;
                if (ueof.getOp() == UnaryExpr.UnaryOp.TRANSPOSE) {
                    fieldName = "~" + fieldName;;
                }
                if (ueof.getOp() != UnaryExpr.UnaryOp.NOOP) {
                    firstNoNOOP = false;
                }
            }
            if (par instanceof BinaryExpr) {
                BinaryExpr bopar = (BinaryExpr) par;
                if (bopar.getOp() == BinaryExpr.BinaryOp.JOIN) {
                    try {
                        Node left = bopar.getLeft();
                        if (left instanceof SigExpr) {
                            SigExpr sig = (SigExpr) bopar.getLeft();
                            return asgv.getFieldMap().get(sig.getName()).get(fieldName);
                        } else if (left instanceof VarExpr) {
                            VarExpr var = (VarExpr) left;
                            Pair<Integer, Map<String, Integer>> scope = getScope(var, var.getName(), asgv.getNodeMap(), asgv.getScopeRelMap());
                            Node scopeStarter = asgv.getNodeMap().get(scope.a);
                            for (Node child : scopeStarter.getChildren()) {
                                if (child instanceof VarDecl) {
                                    VarDecl vd = (VarDecl) child;
                                    if (vd.getNames().contains(var.getName())) {
                                        SigExpr sig = findSigUnderNode(child);
                                        if (sig != null) {
                                            String sigName = sig.getName();
                                            // System.out.println("signame: " + sigName + ", field: " + fieldName + ", in fieldmap: " + asgv.getFieldMap());
                                            Map<String, Map<String, Integer>> fieldMap = asgv.getFieldMap();
                                            Map<String, Integer> currentSigScopeMap = fieldMap.get(sigName);
                                            Map<String, String> subsigDict = asgv.getSubsigDict();
                                            boolean continueFlag = false;
                                            while (currentSigScopeMap == null || !currentSigScopeMap.containsKey(fieldName)) {
                                                sigName = subsigDict.get(sigName);
                                                if (sigName == null) {
                                                    continueFlag = true;
                                                    break;
                                                }
                                                currentSigScopeMap = asgv.getFieldMap().get(sigName);
                                            }
                                            if (continueFlag) {
                                                continue;
                                            }
                                            // System.out.println("signame: " + sigName + ", field: " + fieldName + ", in fieldmap: " + asgv.getFieldMap() + ", currentSigScopeMap: " + currentSigScopeMap);
                                            return asgv.getFieldMap()
                                                .get(sigName)
                                                    .get(fieldName);
                                        }
                                    }    
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
            par = par.getParent();
        }
        ModelUnit mu = (ModelUnit) par;
        int result = -1;
        for (Node n : mu.getChildren()) {
            if (n instanceof SigDecl) {
                SigDecl sig = (SigDecl) n;
                Map<String, Integer> sigFieldMap = asgv.getFieldMap().get(sig.getName());
                if (!(sigFieldMap == null) && (sigFieldMap.containsKey(fe.getName()))) {
                    result = sigFieldMap.get(fe.getName());
                    // not break here for shadowing
                }
            }
        }
        if (result == -1) {
            System.out.println("Error: cannot find scope for " + fe.getName() + ", cannot process;");
            throw new ScopeNotFoundException(fe.getName(), fe);
        }
        return result;
    }

    private SigExpr findSigUnderNode(Node vd) {
        for (Node n : vd.getChildren()) {
            if (n instanceof SigExpr) {
                return (SigExpr) n;
            } else {
                SigExpr sig = findSigUnderNode(n);
                if (sig != null) {
                    return sig;
                }
            }
        }
        return null;
    }
}
