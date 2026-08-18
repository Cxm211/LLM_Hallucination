private Node tryRemoveUnconditionalBranching(Node n) {
    if (n == null) {
       return n;
    }

    DiGraphNode<Node, Branch> gNode = curCfg.getDirectedGraphNode(n);

    if (gNode == null) {
      return n;
    }

    if (n.getParent() == null) {
      List<DiGraphEdge<Node,Branch>> outEdges = gNode.getOutEdges();
      if (outEdges.size() == 1) {
        return tryRemoveUnconditionalBranching(outEdges.get(0).getDestination().getValue());
      }
    }
    switch (n.getType()) {
      case Token.BLOCK:
        if (n.hasChildren()) {
          Node first = n.getFirstChild();
          return tryRemoveUnconditionalBranching(first);
        } else {
          return tryRemoveUnconditionalBranching(ControlFlowAnalysis.computeFollowNode(n));
        }
      case Token.RETURN:
      case Token.BREAK:
      case Token.CONTINUE:
        List<DiGraphEdge<Node,Branch>> outEdges = gNode.getOutEdges();
        if (outEdges.size() == 1 &&
            (n.getNext() == null || n.getNext().getType() == Token.FUNCTION)) {
          Preconditions.checkState(outEdges.get(0).getValue() == Branch.UNCOND);
          Node fallThrough = tryRemoveUnconditionalBranching(computeFollowing(n));
          Node nextCfgNode = outEdges.get(0).getDestination().getValue();
          if (nextCfgNode == fallThrough) {
            removeDeadExprStatementSafely(n);
            return fallThrough;
          }
        }
    }
    return n;
  }