  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }