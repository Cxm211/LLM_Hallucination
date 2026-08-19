// ===== FIXED com.google.javascript.rhino.Node :: getChildBefore(Node) [lines 549-565] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-110-fixed/src/com/google/javascript/rhino/Node.java =====
  public Node getChildBefore(Node child) {
    if (child == first) {
      return null;
    }
    Node n = first;
    if (n == null) {
      throw new RuntimeException("node is not a child");
    }

    while (n.next != child) {
      n = n.next;
      if (n == null) {
        throw new RuntimeException("node is not a child");
      }
    }
    return n;
  }
