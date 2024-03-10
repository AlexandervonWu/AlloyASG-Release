package is.fivefivefive.alloyasg.exceptions;

import parser.ast.nodes.Node;

public class ScopeNotFoundException extends Exception {
    public ScopeNotFoundException(String name, Node n) {
        super("Scope not found for the " + n.getClass().toString() + " with name " + name + ".");
    }
}
