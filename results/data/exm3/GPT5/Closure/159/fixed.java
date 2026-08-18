// ===== FIXED com.google.javascript.jscomp.InlineFunctions :: findCalledFunctions(Node, Set) [lines 773-786] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-159-fixed/src/com/google/javascript/jscomp/InlineFunctions.java =====
  private void findCalledFunctions(
      Node node, Set<String> changed) {
    Preconditions.checkArgument(changed != null);
    // For each referenced function, add a new reference
    if (node.getType() == Token.NAME) {
      if (isCandidateUsage(node)) {
        changed.add(node.getString());
      }
    }

    for (Node c = node.getFirstChild(); c != null; c = c.getNext()) {
      findCalledFunctions(c, changed);
    }
  }
