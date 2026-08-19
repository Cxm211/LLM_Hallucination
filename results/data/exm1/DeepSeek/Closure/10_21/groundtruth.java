// ===== FIXED com.google.javascript.jscomp.NodeUtil :: mayBeString(Node, boolean) [lines 1415-1421] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-10-fixed/src/com/google/javascript/jscomp/NodeUtil.java =====
  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return anyResultsMatch(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }
