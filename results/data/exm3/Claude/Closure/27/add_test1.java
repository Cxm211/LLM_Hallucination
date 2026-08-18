// com/google/javascript/rhino/IRTest.java
public void testTryCatchWithMultipleStatements() {
  testIR(
      IR.tryCatch(
          IR.block(IR.exprResult(IR.string("test"))),
          IR.catchNode(
              IR.name("ex"),
              IR.block(IR.exprResult(IR.name("ex"))))),
      "TRY\n" +
      "    BLOCK\n" +
      "        EXPR_RESULT\n" +
      "            STRING test\n" +
      "    BLOCK\n" +
      "        CATCH\n" +
      "            NAME ex\n" +
      "            BLOCK\n" +
      "                EXPR_RESULT\n" +
      "                    NAME ex\n");
}