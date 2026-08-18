private void recordAssignment(NodeTraversal t, Node n, Node recordNode) {
  Node nameNode = n.getFirstChild();
  Node parent = n.getParent();
  NameInformation ns = createNameInformation(t, nameNode);
  if (ns != null) {
    Node forLoop = null;
    for (Node current = parent; current != null; current = current.getParent()) {
      if (current.isFor() && !NodeUtil.isForIn(current)) {
        forLoop = current;
        break;
      }
    }
    if (forLoop != null) {
      Node condition = forLoop.getFirstChild().getNext();
      boolean inCondition = false;
      if (condition != null) {
        for (Node p = n; p != null; p = p.getParent()) {
          if (p == condition) {
            inCondition = true;
            break;
          }
        }
      }
      if (inCondition) {
        recordDepScope(nameNode, ns);
      } else {
        recordDepScope(recordNode, ns);
      }
    } else {
      if (parent.isFor() && !NodeUtil.isForIn(parent)) {
        if (parent.getFirstChild().getNext() != n) {
          recordDepScope(recordNode, ns);
        } else {
          recordDepScope(nameNode, ns);
        }
      } else {
        recordDepScope(recordNode, ns);
      }
    }
  }
}