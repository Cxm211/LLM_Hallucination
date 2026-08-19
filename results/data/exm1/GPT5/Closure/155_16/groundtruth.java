// ===== FIXED com.google.javascript.jscomp.ReferenceCollectingCallback :: visit(NodeTraversal, Node, Node) [lines 121-138] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-155-fixed/src/com/google/javascript/jscomp/ReferenceCollectingCallback.java =====
  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.getType() == Token.NAME) {
      Var v;
      if (n.getString().equals("arguments")) {
        v = t.getScope().getArgumentsVar();
      } else {
        v = t.getScope().getVar(n.getString());
      }
      if (v != null && varFilter.apply(v)) {
        addReference(t, v,
            new Reference(n, parent, t, blockStack.peek()));
      }
    }

    if (isBlockBoundary(n, parent)) {
      blockStack.pop();
    }
  }
