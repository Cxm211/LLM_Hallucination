  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }