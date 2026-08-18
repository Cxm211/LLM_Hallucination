  public void testFunctionInference11() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): number");
  }