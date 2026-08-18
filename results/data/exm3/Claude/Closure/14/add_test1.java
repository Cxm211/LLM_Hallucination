// com/google/javascript/jscomp/ControlFlowAnalysisTest.java
public void testNestedCatchWithFinally() {
    String src = "try { try { var a; } catch(e) { var b; } finally { var c; } } finally { var d; }";
    ControlFlowGraph<Node> cfg = createCfg(src);
    assertDownEdge(cfg, Token.TRY, Token.BLOCK, Branch.UNCOND);
    assertCrossEdge(cfg, Token.VAR, Token.BLOCK, Branch.ON_EX);
    assertCrossEdge(cfg, Token.VAR, Token.BLOCK, Branch.UNCOND);
  }