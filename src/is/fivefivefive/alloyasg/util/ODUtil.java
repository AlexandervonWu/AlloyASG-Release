package is.fivefivefive.alloyasg.util;

import is.fivefivefive.alloyasg.asg.PredSubgraph;

public class ODUtil {
    /**
     * Get the maximum number of children for a given syntax ID.
     * 0 means it is a leaf node. 
     * @param syntactic the syntax ID
     * @return the maximum number of children
     */
    public static int getMaxChildrenBySyntax(int syntactic) {
        switch (syntactic) {
            case 0: // OverallRoot, only one child
                return 1;
            case 1: // SigExpr
                return 0;
            case 2: // VarExpr
                return 0;
            case -2: // RelDecl, infinite
                return 17;
            case 3: // FieldExpr
                return 0;
            case 4: // ConstExpr - Integers
                return 0;
            case 5: // ConstExpr - Boolean
                return 0;
            case 6: // CallExpr
                return 1;
            case -6: // CallFormula
                return 1;
            case 7: // UnaryExpr
                return 1;
            case -7: // UnaryFormula
                return 1;
            case 8: // BinaryExpr
                return 2;
            case -8: // BinaryFormula
                return 2;
            case 9: // ListExpr, infinite
                return 17;
            case -9: // ListFormula, infinite
                return 17;
            case 10: // LetExpr
                return 3;
            case 11: // QtExpr, infinite
                return 17;
            case -11: // QtFormula, infinite
                return 17;
            case 12: // ITEExpr
                return 3;
            case -12: // ITEFormula
                return 3;
        }
        // 17 by default, order for infinite
        return PredSubgraph.ORDER_DIVISOR;
    }
}
