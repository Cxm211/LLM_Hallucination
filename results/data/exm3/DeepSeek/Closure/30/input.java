// buggy function
  public void process(Node externs, Node root) {
    (new NodeTraversal(compiler, this)).traverse(root);
  }

  MustBeReachingVariableDef(
      ControlFlowGraph<Node> cfg, Scope jsScope, AbstractCompiler compiler) {
    super(cfg, new MustDefJoin());
    this.jsScope = jsScope;
    this.compiler = compiler;
    this.escaped = Sets.newHashSet();
    computeEscaped(jsScope, escaped, compiler);
  }

  private void computeDependence(final Definition def, Node rValue) {
    NodeTraversal.traverse(compiler, rValue,
        new AbstractCfgNodeTraversalCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isName() && jsScope.isDeclared(n.getString(), true)) {
          Var dep = jsScope.getVar(n.getString());
            def.depends.add(dep);
        }
      }
    });
  }

  boolean dependsOnOuterScopeVars(String name, Node useNode) {
    Preconditions.checkArgument(getCfg().hasNode(useNode));
    GraphNode<Node, Branch> n = getCfg().getNode(useNode);
    FlowState<MustDef> state = n.getAnnotation();
    Definition def = state.getIn().reachingDef.get(jsScope.getVar(name));

    for (Var s : def.depends) {
      if (s.scope != jsScope) {
        return true;
      }
    }
    return false;
  }

// trigger testcase
// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testCanInlineAcrossNoSideEffect
public void testCanInlineAcrossNoSideEffect() {
    // This can't be inlined because print() has side-effects and might change
    // the definition of noSFX. We should be able to mark noSFX as const
    // in some way.
    noInline(
        "var y; var x = noSFX(y), z = noSFX(); noSFX(); noSFX(), print(x)");
    //inline(
    //    "var y; var x = noSFX(y), z = noSFX(); noSFX(); noSFX(), print(x)",
    //    "var y; var x, z = noSFX(); noSFX(); noSFX(), print(noSFX(y))");
  }

// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testInlineAcrossSideEffect1
public void testInlineAcrossSideEffect1() {
    // This can't be inlined because print() has side-effects and might change
    // the definition of noSFX.
    //
    // noSFX must be both const and pure in order to inline it.
    noInline("var y; var x = noSFX(y); print(x)");
    //inline("var y; var x = noSFX(y); print(x)", "var y;var x;print(noSFX(y))");
  }

// com/google/javascript/jscomp/FlowSensitiveInlineVariablesTest.java::testIssue698
public void testIssue698() {
    // Most of the flow algorithms operate on Vars. We want to make
    // sure the algorithm bails out appropriately if it sees
    // a var that it doesn't know about.
    inline(
        "var x = ''; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;",
        "var x; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = '' + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;");
  }
