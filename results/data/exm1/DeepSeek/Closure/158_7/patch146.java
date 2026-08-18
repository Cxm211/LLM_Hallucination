  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }