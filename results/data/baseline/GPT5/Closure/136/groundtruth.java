// ===== FIXED com.google.javascript.jscomp.MethodCompilerPass :: addPossibleSignature(String, Node, NodeTraversal) [lines 101-108] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-136-fixed/src/com/google/javascript/jscomp/MethodCompilerPass.java =====
  private void addPossibleSignature(String name, Node node, NodeTraversal t) {
    if (node.getType() == Token.FUNCTION) {
      // The node we're looking at is a function, so we can add it directly
      addSignature(name, node, t.getSourceName());
    } else {
      nonMethodProperties.add(name);
    }
  }
