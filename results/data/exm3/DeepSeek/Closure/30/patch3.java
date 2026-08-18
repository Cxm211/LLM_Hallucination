  boolean dependsOnOuterScopeVars(String name, Node useNode) {
    Preconditions.checkArgument(getCfg().hasNode(useNode));
    GraphNode<Node, Branch> n = getCfg().getNode(useNode);
    FlowState<MustDef> state = n.getAnnotation();
    Var var = jsScope.getVar(name);
    if (var == null) {
      return false;
    }
    Definition def = state.getIn().reachingDef.get(var);
    if (def == null) {
      return false;
    }
    for (Var s : def.depends) {
      if (s.scope != jsScope) {
        return true;
      }
    }
    return false;
  }