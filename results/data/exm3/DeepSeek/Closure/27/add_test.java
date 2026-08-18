// com/google/javascript/rhino/IRTest.java
public void testTryFinallyWithNonEmptyBlocks() {
    testIR(
        IR.tryFinally(
            IR.block(IR.returnNode()),
            IR.block(IR.breakNode())),
        "TRY\n" +
        "    BLOCK\n" +
        "        RETURN\n" +
        "    BLOCK\n" +
        "    BLOCK\n" +
        "        BREAK\n");
  }
