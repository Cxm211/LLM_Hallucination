  boolean dependsOnOuterScopeVars(String name, Node useNode) {
    Preconditions.checkArgument(getCfg().hasNode(useNode));
    GraphNode<Node, Branch> n = getCfg().getNode(useNode);
    FlowState<MustDef> state = n.getAnnotation();
    Var var = jsScope.getVar(name);
    Definition def = null;
    if (state != null && state.getIn() != null && var != null && state.getIn().reachingDef != null) {
      def = state.getIn().reachingDef.get(var);
    }

    if (def == null || def.depends == null) {
      return false;
    }

    for (Var s : def.depends) {
      if (s != null && s.scope != jsScope) {
        return true;
      }
    }
    return false;
  }