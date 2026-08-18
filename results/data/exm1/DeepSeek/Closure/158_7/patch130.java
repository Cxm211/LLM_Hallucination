  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }