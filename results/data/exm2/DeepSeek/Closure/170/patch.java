private void getNumUseInUseCfgNode(final Node cfgNode) {

  numUsesWithinCfgNode = 0;
  AbstractCfgNodeTraversalCallback gatherCb =
      new AbstractCfgNodeTraversalCallback() {

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isName() && n.getString().equals(varName)) {
          // Don't count lhs of top-level assignment chain
          numUsesWithinCfgNode++;
      }
    }

  };

  NodeTraversal.traverse(compiler, cfgNode, gatherCb);
}