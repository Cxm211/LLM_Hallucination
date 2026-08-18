// ===== FIXED com.google.javascript.jscomp.FlowSensitiveInlineVariables :: process(Node, Node) [lines 156-158] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-30-fixed/src/com/google/javascript/jscomp/FlowSensitiveInlineVariables.java =====
  public void process(Node externs, Node root) {
    (new NodeTraversal(compiler, this)).traverseRoots(externs, root);
  }

// ===== FIXED com.google.javascript.jscomp.MustBeReachingVariableDef :: computeDependence(Definition, Node) [lines 392-407] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-30-fixed/src/com/google/javascript/jscomp/MustBeReachingVariableDef.java =====
  private void computeDependence(final Definition def, Node rValue) {
    NodeTraversal.traverse(compiler, rValue,
        new AbstractCfgNodeTraversalCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isName()) {
          Var dep = jsScope.getVar(n.getString());
          if (dep == null) {
            def.unknownDependencies = true;
          } else {
            def.depends.add(dep);
          }
        }
      }
    });
  }

// ===== FIXED com.google.javascript.jscomp.MustBeReachingVariableDef :: dependsOnOuterScopeVars(String, Node) [lines 430-445] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-30-fixed/src/com/google/javascript/jscomp/MustBeReachingVariableDef.java =====
  boolean dependsOnOuterScopeVars(String name, Node useNode) {
    Preconditions.checkArgument(getCfg().hasNode(useNode));
    GraphNode<Node, Branch> n = getCfg().getNode(useNode);
    FlowState<MustDef> state = n.getAnnotation();
    Definition def = state.getIn().reachingDef.get(jsScope.getVar(name));
    if (def.unknownDependencies) {
      return true;
    }

    for (Var s : def.depends) {
      if (s.scope != jsScope) {
        return true;
      }
    }
    return false;
  }

// ===== FIXED com.google.javascript.jscomp.MustBeReachingVariableDef :: visit(NodeTraversal, Node, Node) [lines 396-405] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-30-fixed/src/com/google/javascript/jscomp/MustBeReachingVariableDef.java =====
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isName()) {
          Var dep = jsScope.getVar(n.getString());
          if (dep == null) {
            def.unknownDependencies = true;
          } else {
            def.depends.add(dep);
          }
        }
      }
