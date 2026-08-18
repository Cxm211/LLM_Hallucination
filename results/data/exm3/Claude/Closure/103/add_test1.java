// com/google/javascript/jscomp/ControlFlowAnalysisTest.java
public void testInstanceOfInCondition() {
    String src = "if (x instanceof Foo) { var y = 1; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.IF, Token.BLOCK, Branch.ON_TRUE);
  }