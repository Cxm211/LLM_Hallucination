  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }