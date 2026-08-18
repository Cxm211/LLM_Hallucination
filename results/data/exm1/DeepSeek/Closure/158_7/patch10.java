  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }