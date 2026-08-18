// ===== FIXED com.google.javascript.rhino.IR :: block(Node...) [lines 102-109] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-27-fixed/src/com/google/javascript/rhino/IR.java =====
  public static Node block(Node ... stmts) {
    Node block = new Node(Token.BLOCK);
    for (Node stmt : stmts) {
      Preconditions.checkState(mayBeStatement(stmt));
      block.addChildToBack(stmt);
    }
    return block;
  }

// ===== FIXED com.google.javascript.rhino.IR :: tryCatch(Node, Node) [lines 233-238] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-27-fixed/src/com/google/javascript/rhino/IR.java =====
  public static Node tryCatch(Node tryBody, Node catchNode) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(catchNode.isCatch());
    Node catchBody = blockUnchecked(catchNode).copyInformationFrom(catchNode);
    return new Node(Token.TRY, tryBody, catchBody);
  }

// ===== FIXED com.google.javascript.rhino.IR :: tryFinally(Node, Node) [lines 226-231] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-27-fixed/src/com/google/javascript/rhino/IR.java =====
  public static Node tryFinally(Node tryBody, Node finallyBody) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(finallyBody.isBlock());
    Node catchBody = block().copyInformationFrom(tryBody);
    return new Node(Token.TRY, tryBody, catchBody, finallyBody);
  }
