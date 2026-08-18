  private Node computeFollowing(Node n) {
    Node next = n.getNext();
    while (next != null && next.getType() == Token.FUNCTION) {
      next = next.getNext();
    }
    if (next != null) {
      return next;
    }
    return ControlFlowAnalysis.computeFollowNode(n);
  }
