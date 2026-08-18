public boolean apply(Node n) {
  if (n == null) {
    return false;
  }
  if (NodeUtil.mayHaveSideEffects(n)) {
    return true;
  }
  for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
    if (!ControlFlowGraph.isEnteringNewCfgNode(c) && apply(c)) {
      return true;
    }
  }
  return false;
}