    private void getNumUseInUseCfgNode(final Node cfgNode) {

      numUsesWithinCfgNode = 0;
      AbstractCfgNodeTraversalCallback gatherCb =
          new AbstractCfgNodeTraversalCallback() {

        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          if (n.isName() && n.getString().equals(varName)) {
            if (parent.isAssign() && parent.getFirstChild() == n) {
              // lhs of assignment
              // Check if this assignment is inside a hook (conditional expression)
              Node ancestor = parent;
              boolean insideHook = false;
              while (ancestor != null) {
                if (ancestor.isHook()) {
                  insideHook = true;
                  break;
                }
                if (ancestor == cfgNode) {
                  break;
                }
                ancestor = ancestor.getParent();
              }
              if (insideHook) {
                // Count the lhs when inside a hook, because the assignment is conditional
                // and we want to prevent inlining in such cases.
                numUsesWithinCfgNode++;
              }
              // otherwise skip (do not count lhs of non-hook assignments)
            } else {
              // Not the lhs of an assignment, or not an assignment at all: count as a use.
              numUsesWithinCfgNode++;
            }
          }
        }

      };

      NodeTraversal.traverse(compiler, cfgNode, gatherCb);
    }