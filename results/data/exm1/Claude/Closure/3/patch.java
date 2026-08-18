public void enterScope(NodeTraversal t) {

    if (t.inGlobalScope()) {
      return;
    }

    if (LiveVariablesAnalysis.MAX_VARIABLES_TO_ANALYZE <
        t.getScope().getVarCount()) {
      return;
    }

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    Preconditions.checkState(t.getScopeRoot().isFunction());
    cfa.process(null, t.getScopeRoot().getLastChild());
    cfg = cfa.getCfg();
    reachingDef = new MustBeReachingVariableDef(cfg, t.getScope(), compiler);
    reachingDef.analyze();
    candidates = Lists.newLinkedList();

    new NodeTraversal(compiler, new GatherCandiates()).traverse(
        t.getScopeRoot().getLastChild());

    reachingUses = new MaybeReachingVariableUse(cfg, t.getScope(), compiler);
    reachingUses.analyze();
    for (Candidate c : candidates) {
      if (c.canInline()) {
        c.inlineVariable();

        if (!c.defMetadata.depends.isEmpty()) {
          inlinedNewDependencies.add(t.getScope().getVar(c.varName));
        }
      }
    }
  }