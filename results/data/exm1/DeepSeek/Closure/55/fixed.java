// ===== FIXED com.google.javascript.jscomp.FunctionRewriter :: isReduceableFunctionExpression(Node) [lines 116-119] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-55-fixed/src/com/google/javascript/jscomp/FunctionRewriter.java =====
  private static boolean isReduceableFunctionExpression(Node n) {
    return NodeUtil.isFunctionExpression(n)
        && !NodeUtil.isGetOrSetKey(n.getParent());
  }


  