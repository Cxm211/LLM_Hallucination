// buggy function
  public static Node block(Node ... stmts) {
    Node block = new Node(Token.BLOCK);
    for (Node stmt : stmts) {
      Preconditions.checkState(mayBeStatement(stmt));
      block.addChildToBack(stmt);
    }
    return block;
  }

  public static Node tryFinally(Node tryBody, Node finallyBody) {
    Preconditions.checkState(tryBody.isLabelName());
    Preconditions.checkState(finallyBody.isLabelName());
    Node catchBody = block().copyInformationFrom(tryBody);
    return new Node(Token.TRY, tryBody, catchBody, finallyBody);
  }

  public static Node tryCatch(Node tryBody, Node catchNode) {
    Preconditions.checkState(tryBody.isBlock());
    Preconditions.checkState(catchNode.isCatch());
    Node catchBody = block(catchNode).copyInformationFrom(catchNode);
    return new Node(Token.TRY, tryBody, catchBody);
  }

// trigger testcase
// com/google/javascript/rhino/IRTest.java::testIssue727_1
public void testIssue727_1() {
    testIR(
        IR.tryFinally(
            IR.block(),
            IR.block()),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "    BLOCK\n");
  }

// com/google/javascript/rhino/IRTest.java::testIssue727_2
public void testIssue727_2() {
    testIR(
        IR.tryCatch(
            IR.block(),
            IR.catchNode(
                IR.name("e"),
                IR.block())),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "        CATCH\n" +
        "            NAME e\n" +
        "            BLOCK\n");
  }

// com/google/javascript/rhino/IRTest.java::testIssue727_3
public void testIssue727_3() {
    testIR(
        IR.tryCatchFinally(
            IR.block(),
            IR.catchNode(IR.name("e"), IR.block()),
            IR.block()),
        "TRY\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "        CATCH\n" +
        "            NAME e\n" +
        "            BLOCK\n" +
        "    BLOCK\n");
  }
