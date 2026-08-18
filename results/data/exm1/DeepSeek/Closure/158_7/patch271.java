  public void testEnum36() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return 1; }",
              "inconsistent return type\n" +
              "found   : number\n" +
              "required: a.b.<number>");
  }