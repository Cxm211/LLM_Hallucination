  public void testInnerFunction2() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = null;\n" +
        " function g() { x = 3; }" +
        " g();" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : (null|number)\n" +
        "required: number");
  }