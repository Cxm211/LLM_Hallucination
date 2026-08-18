  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?): string");
  }