// ===== FIXED com.google.javascript.jscomp.ExploitAssigns :: isSafeReplacement(Node, Node) [lines 206-222] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-124-fixed/src/com/google/javascript/jscomp/ExploitAssigns.java =====
  private boolean isSafeReplacement(Node node, Node replacement) {
    // No checks are needed for simple names.
    if (node.isName()) {
      return true;
    }
    Preconditions.checkArgument(node.isGetProp());

    while (node.isGetProp()) {
      node = node.getFirstChild();
    }
    if (node.isName()
        && isNameAssignedTo(node.getString(), replacement)) {
      return false;
    }

    return true;
  }
