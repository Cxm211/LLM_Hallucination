private void getNumUseInUseCfgNode(final Node cfgNode) {

      numUsesWithinCfgNode = 0;
      AbstractCfgNodeTraversalCallback gatherCb =
          new AbstractCfgNodeTraversalCallback() {

        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          if (n.isName() && n.getString().equals(varName)) {
            boolean isLhsOfAssign = parent != null && parent.isAssign() && parent.getFirstChild() == n;
            if (isLhsOfAssign) {
              // Only ignore LHS if the assignment (or a chain of assignments)
              // constitutes the entire cfgNode (possibly wrapped in EXPR_RESULT).
              Node assignTop = parent;
              // Climb through a chain of assignments where the current node is the first child.
              while (assignTop.getParent() != null && assignTop.getParent().isAssign() && assignTop.getParent().getFirstChild() == assignTop) {
                assignTop = assignTop.getParent();
              }
              Node topParent = assignTop.getParent();
              boolean entireNodeIsAssignChain = (assignTop == cfgNode) || (topParent != null && topParent == cfgNode) || (topParent != null && topParent.isExprResult() && topParent == cfgNode);
              if (!entireNodeIsAssignChain) {
                // Count LHS as a use when the assignment is embedded in a larger expression.
                numUsesWithinCfgNode++;
              }
            } else {
              numUsesWithinCfgNode++;
            }
          }
        }

      };

      NodeTraversal.traverse(compiler, cfgNode, gatherCb);
    }