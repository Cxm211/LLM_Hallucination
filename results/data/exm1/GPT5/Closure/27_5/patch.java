public static Node block(Node ... stmts) {
    Node block = new Node(Token.BLOCK);
    for (Node stmt : stmts) {
      if (!mayBeStatement(stmt)) {
        stmt = new Node(Token.EXPR_RESULT, stmt);
      }
      Preconditions.checkState(mayBeStatement(stmt));
      block.addChildToBack(stmt);
    }
    return block;
  }