      public boolean apply(Node n) {
        // When the node is null it means, we reached the implicit return
        // where the function returns (possibly without an return statement)
        if (n == null) {
          return false;
        }

        // Consider any node that may have side effects, not just calls/news.
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