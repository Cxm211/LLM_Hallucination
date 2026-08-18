// com/google/javascript/jscomp/ControlFlowAnalysisTest.java
public void testContinueInTryFinally() {
    String src = "while (1) { try { continue; } finally {} 1; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    // continue -> finally block
    assertCrossEdge(cfg, Token.CONTINUE, Token.BLOCK, Branch.UNCOND);
    // finally block -> while (the loop condition)
    assertCrossEdge(cfg, Token.BLOCK, Token.WHILE, Branch.UNCOND);
  }
