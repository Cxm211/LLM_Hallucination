// ===== FIXED com.google.javascript.jscomp.ClosureCodingConvention :: extractClassNameIfGoog(Node, Node, String) [lines 188-204] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-57-fixed/src/com/google/javascript/jscomp/ClosureCodingConvention.java =====
  private static String extractClassNameIfGoog(Node node, Node parent,
      String functionName){
    String className = null;
    if (NodeUtil.isExprCall(parent)) {
      Node callee = node.getFirstChild();
      if (callee != null && callee.getType() == Token.GETPROP) {
        String qualifiedName = callee.getQualifiedName();
        if (functionName.equals(qualifiedName)) {
          Node target = callee.getNext();
          if (target != null && target.getType() == Token.STRING) {
            className = target.getString();
          }
        }
      }
    }
    return className;
  }
