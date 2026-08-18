// com/google/javascript/jscomp/ControlFlowAnalysisTest.java
public void testInstanceOfNested() {
    String src = "try { x instanceof y instanceof z } catch (e) { }";
    ControlFlowGraph<Node> cfg = createCfg(src, true);
    assertCrossEdge(cfg, Token.EXPR_RESULT, Token.BLOCK, Branch.ON_EX);
  }
