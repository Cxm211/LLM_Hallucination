private boolean canInline() {
      if (getDefCfgNode().isFunction()) {
        return false;
      }

      for (Var dependency : defMetadata.depends) {
        if (inlinedNewDependencies.contains(dependency)) {
          return false;
        }
      }

      getDefinition(getDefCfgNode(), null);
      getNumUseInUseCfgNode(useCfgNode, null);

      if (def == null) {
        return false;
      }

      if (def.isAssign() && !NodeUtil.isExprAssign(def.getParent())) {
        return false;
      }

      if (checkRightOf(def, getDefCfgNode(), SIDE_EFFECT_PREDICATE)) {
        return false;
      }

      if (checkLeftOf(use, useCfgNode, SIDE_EFFECT_PREDICATE)) {
        return false;
      }

      if (NodeUtil.mayHaveSideEffects(def.getLastChild(), compiler)) {
        return false;
      }

      if (numUseWithinUseCfgNode != 1) {
        return false;
      }

      if (NodeUtil.isWithinLoop(use)) {
        return false;
      }


      Collection<Node> uses = reachingUses.getUses(varName, getDefCfgNode());

      if (uses.size() != 1) {
        return false;
      }

      if (NodeUtil.has(def.getLastChild(),
          new Predicate<Node>() {
              @Override
              public boolean apply(Node input) {
                switch (input.getType()) {
                  case Token.GETELEM:
                  case Token.GETPROP:
                  case Token.ARRAYLIT:
                  case Token.OBJECTLIT:
                  case Token.REGEXP:
                  case Token.NEW:
                    return true;
                }
                return false;
              }
          },
          new Predicate<Node>() {
              @Override
              public boolean apply(Node input) {
                return !input.isFunction();
              }
          })) {
        return false;
      }

      if (NodeUtil.isStatementBlock(getDefCfgNode().getParent()) &&
          getDefCfgNode().getNext() != useCfgNode) {
        CheckPathsBetweenNodes<Node, ControlFlowGraph.Branch>
          pathCheck = new CheckPathsBetweenNodes<Node, ControlFlowGraph.Branch>(
                 cfg,
                 cfg.getDirectedGraphNode(getDefCfgNode()),
                 cfg.getDirectedGraphNode(useCfgNode),
                 SIDE_EFFECT_PREDICATE,
                 Predicates.
                     <DiGraphEdge<Node, ControlFlowGraph.Branch>>alwaysTrue(),
                 false);
        if (pathCheck.somePathsSatisfyPredicate()) {
          return false;
        }
      }

      return true;
    }