  public void testFunctionInference19() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (string): ?");
  }