private void recordAssignment(NodeTraversal t, Node n, Node recordNode) {
  Node nameNode = n.getFirstChild();
  Node parent = n.getParent();
  NameInformation ns = createNameInformation(t, nameNode);
  if (ns != null) {
    // The rhs of the assignment is the caller, so it's used by the
    // context. Don't associate it w/ the lhs.
    recordDepScope(recordNode, ns);
  }
}