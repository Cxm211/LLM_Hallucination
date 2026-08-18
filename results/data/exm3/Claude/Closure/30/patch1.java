MustBeReachingVariableDef(
    ControlFlowGraph<Node> cfg, Scope jsScope, AbstractCompiler compiler) {
  super(cfg, new MustDefJoin());
  this.jsScope = jsScope;
  this.compiler = compiler;
  this.escaped = Sets.newHashSet();
  computeEscaped(jsScope, escaped, compiler);
}
