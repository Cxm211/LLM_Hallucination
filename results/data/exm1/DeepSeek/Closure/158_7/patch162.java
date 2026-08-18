  public void testFunctionInference18() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.eatWithDate;",
        "goog.eatWithDate",
        "function (this:Date): ?");
  }