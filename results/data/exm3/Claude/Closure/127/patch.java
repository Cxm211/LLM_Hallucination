private void tryRemoveUnconditionalBranching(Node n) {
  /*
   * For each unconditional branching control flow node, check to see
   * if the ControlFlowAnalysis.computeFollowNode of that node is same as
   * the branching target. If it is, the branch node is safe to be removed.
   *
   * This is not as clever as MinimizeExitPoints because it doesn't do any
   * if-else conversion but it handles more complicated switch statements
   * much more nicely.
   */

  // If n is null the target is the end of the function, nothing to do.
  if (n == null) {
     return;
  }

  DiGraphNode<Node, Branch> gNode = cfg.getDirectedGraphNode(n);

  if (gNode == null) {
    return;
  }

  switch (n.getType()) {
    case Token.RETURN:
      if (n.hasChildren()) {
        break;
      }
    case Token.BREAK:
    case Token.CONTINUE:
      // Don't remove break/continue if they are within a finally block
      if (isInFinallyBlock(n)) {
        break;
      }
      // We are looking for a control flow changing statement that always
      // branches to the same node. If after removing it control still
      // branches to the same node, it is safe to remove.
      List<DiGraphEdge<Node, Branch>> outEdges = gNode.getOutEdges();
      if (outEdges.size() == 1 &&
          // If there is a next node, this jump is not useless.
          (n.getNext() == null || n.getNext().isFunction())) {

        Preconditions.checkState(
            outEdges.get(0).getValue() == Branch.UNCOND);
        Node fallThrough = computeFollowing(n);
        Node nextCfgNode = outEdges.get(0).getDestination().getValue();
        if (nextCfgNode == fallThrough) {
          removeNode(n);
        }
      }
  }
}

private boolean isInFinallyBlock(Node n) {
  for (Node ancestor = n.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
    if (ancestor.isFunction()) {
      return false;
    }
    if (ancestor.isTry()) {
      Node tryNode = ancestor;
      Node catchNode = tryNode.getFirstChild().getNext();
      Node finallyNode = catchNode.getNext();
      if (finallyNode != null) {
        // Check if n is within the finally block
        for (Node current = n; current != tryNode && current != null; current = current.getParent()) {
          if (current == finallyNode) {
            return true;
          }
        }
      }
    }
  }
  return false;
}