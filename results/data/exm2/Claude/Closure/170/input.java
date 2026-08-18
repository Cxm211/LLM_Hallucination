    private void getNumUseInUseCfgNode(final Node cfgNode) {

      numUsesWithinCfgNode = 0;
      AbstractCfgNodeTraversalCallback gatherCb =
          new AbstractCfgNodeTraversalCallback() {

        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          if (n.isName() && n.getString().equals(varName) &&
            // We make a special exception when the entire cfgNode is a chain
            // of assignments, since in that case the assignment statements
            // will happen after the inlining of the right hand side.
            // TODO(blickly): Make the SIDE_EFFECT_PREDICATE check more exact
            //   and remove this special case.
              !(parent.isAssign() &&
               (parent.getFirstChild() == n))) {
              // Don't count lhs of top-level assignment chain
              numUsesWithinCfgNode++;
          }
        }

      };

      NodeTraversal.traverse(compiler, cfgNode, gatherCb);
    }

// trigger testcase
public void testVarAssinInsideHookIssue965() {
    noInline("var i = 0; return 1 ? (i = 5) : 0, i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) ? i : 0;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) || i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) * i;");
  }
