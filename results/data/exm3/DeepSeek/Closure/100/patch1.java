  private boolean shouldReportThis(Node n, Node parent) {
    // Check if this node is on the left side of any assignment.
    for (Node p = n.getParent(); p != null; p = p.getParent()) {
      if (p.getType() == Token.ASSIGN) {
        Node lhs = p.getFirstChild();
        // Check if n is in the subtree of lhs.
        for (Node t = n; t != null; t = t.getParent()) {
          if (t == lhs) {
            return false;
          }
        }
      }
    }
    // Report if it's a property access (GETPROP) and this is the object being accessed.
    if (parent != null && parent.getType() == Token.GETPROP && parent.getFirstChild() == n) {
      return true;
    }
    return false;
  }