  public void testInnerFunction5() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = 3;\n" +
        " " +
        " function g() { var x = 3;x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }