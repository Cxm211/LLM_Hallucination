private void getNumUseInUseCfgNode(final Node cfgNode) {

  numUsesWithinCfgNode = 0;
  AbstractCfgNodeTraversalCallback gatherCb =
      new AbstractCfgNodeTraversalCallback() {

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (n.isName() && n.getString().equals(varName)) {
        // We make a special exception when the entire cfgNode is a chain
        // of assignments, since in that case the assignment statements
        // will happen after the inlining of the right hand side.
        // TODO(blickly): Make the SIDE_EFFECT_PREDICATE check more exact
        //   and remove this special case.
        if (parent.isAssign() && (parent.getFirstChild() == n)) {
          // Don't count lhs of top-level assignment chain
          return;
        }
        // Don't count the variable reference in a for-in loop's iteration variable
        if (NodeUtil.isForIn(parent)) {
          return;
        }
        numUsesWithinCfgNode++;
      }
    }

  };

  NodeTraversal.traverse(compiler, cfgNode, gatherCb);
}