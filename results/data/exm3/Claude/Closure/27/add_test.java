// com/google/javascript/rhino/IRTest.java
public void testTryFinallyWithStatements() {
  testIR(
      IR.tryFinally(
          IR.block(IR.exprResult(IR.number(1))),
          IR.block(IR.exprResult(IR.number(2)))),
      "TRY\n" +
      "    BLOCK\n" +
      "        EXPR_RESULT\n" +
      "            NUMBER 1.0\n" +
      "    BLOCK\n" +
      "    BLOCK\n" +
      "        EXPR_RESULT\n" +
      "            NUMBER 2.0\n");
}