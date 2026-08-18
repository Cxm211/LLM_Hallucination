  public void testFunctionInference20() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (this:Date): ?");
  }