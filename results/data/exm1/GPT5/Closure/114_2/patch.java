private void recordAssignment(NodeTraversal t, Node n, Node recordNode) {
  Node nameNode = n.getFirstChild();
  Node parent = n.getParent();
  NameInformation ns = createNameInformation(t, nameNode);
  if (ns != null) {
    if (parent.isFor() && !NodeUtil.isForIn(parent)) {
      // For assignments in a FOR loop, only the init part should associate
      // with the lhs; condition and iteration should associate with the
      // surrounding context.
      if (parent.getFirstChild() != n) {
        recordDepScope(recordNode, ns);
      } else {
        recordDepScope(nameNode, ns);
      }
    } else {
      // The rhs of the assignment is the caller, so it's used by the context.
      // Don't associate it with the lhs.
      recordDepScope(recordNode, ns);
    }
  }
}