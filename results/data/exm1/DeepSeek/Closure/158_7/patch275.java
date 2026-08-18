  public void testEnum40() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }