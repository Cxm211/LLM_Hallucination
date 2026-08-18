  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }