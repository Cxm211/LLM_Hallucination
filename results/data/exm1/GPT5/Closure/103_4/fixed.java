// ===== FIXED com.google.javascript.jscomp.ControlFlowAnalysis :: mayThrowException(Node) [lines 884-905] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-103-fixed/src/com/google/javascript/jscomp/ControlFlowAnalysis.java =====
  private static boolean mayThrowException(Node n) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.GETPROP:
      case Token.GETELEM:
      case Token.THROW:
      case Token.NEW:
      case Token.ASSIGN:
      case Token.INC:
      case Token.DEC:
      case Token.INSTANCEOF:
        return true;
      case Token.FUNCTION:
        return false;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (!ControlFlowGraph.isEnteringNewCfgNode(c) && mayThrowException(c)) {
        return true;
      }
    }
    return false;
  }
