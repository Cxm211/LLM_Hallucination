public static Node block(Node ... stmts) {
  Node block = new Node(Token.BLOCK);
  for (Node stmt : stmts) {
    // Allow any node as a child of a BLOCK, since some BLOCKs (like the catch container)
    // may contain nodes (e.g., CATCH) that are not standalone statements.
    Preconditions.checkNotNull(stmt);
    block.addChildToBack(stmt);
  }
  return block;
}
